package com.example.demo.model;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileDTO {
    boolean isDir;
    String objectName;
    String objectNameWeb;
    long size;
    ZonedDateTime lastModified;

    public FileDTO(Result<Item> result) throws Exception {
        var item = result.get();
        this.isDir = item.isDir();
        this.objectName = item.objectName();
        if (isDir) {
            this.objectNameWeb = objectName.substring(0, objectName.lastIndexOf('/'));
            this.objectNameWeb = objectNameWeb.substring(objectName.lastIndexOf('/' + 1));
        } else this.objectNameWeb = item.objectName().substring(item.objectName().lastIndexOf("/"));
        this.size = item.size();
        this.lastModified = item.lastModified();
    }

    public static List<FileDTO> getFileDTOList(Iterable<Result<Item>> itemList) throws Exception {
        var result = new ArrayList<FileDTO>();
        for (Result<Item> itemResult : itemList) {
            result.add(new FileDTO(itemResult));
        }
        return result;
    }

    public String getLastModifiedAsString() {
        return lastModified.format(DateTimeFormatter.ofPattern("MM.dd.yy HH:mm"));
    }
}
