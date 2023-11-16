package com.example.demo.model;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Data
public class FileDTO {
    boolean isDir;
    String objectName;
    String objectNameWeb;
    Long size;
    String lastModified;
    String type;
    boolean isHidden;

    public FileDTO(Result<Item> result) throws Exception {
        var item = result.get();
        this.isDir = item.isDir();
        this.objectName = item.objectName();
        if (isDir) {
            this.objectNameWeb = objectName.substring(0, objectName.lastIndexOf('/'));
            this.objectNameWeb = objectNameWeb.substring(objectNameWeb.lastIndexOf('/') + 1);
        } else this.objectNameWeb = item.objectName().substring(item.objectName().lastIndexOf("/") + 1);
        this.size = item.size();
        if (!isDir) {
            this.lastModified = item.lastModified().format(DateTimeFormatter.ofPattern("MM.dd.yy HH:mm"));
        } else lastModified = "";
        if (isDir) {
            type = "";
        } else type = objectNameWeb.substring(objectNameWeb.lastIndexOf('.') + 1);
        if (!isDir && objectName.endsWith("/")) {
            isHidden = true;
        }
    }

    public static ArrayList<FileDTO> getFileDTOList(Iterable<Result<Item>> itemList) throws Exception {
        var result = new ArrayList<FileDTO>();
        for (Result<Item> itemResult : itemList) {
            var file = new FileDTO(itemResult);
            if (!file.isHidden) {
                result.add(file);
            }
        }
        return result;
    }
}
