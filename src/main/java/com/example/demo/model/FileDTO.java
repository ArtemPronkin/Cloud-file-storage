package com.example.demo.model;

import io.minio.Result;
import io.minio.messages.Item;
import lombok.Data;
import lombok.SneakyThrows;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileDTO {
    boolean isDir;
    String objectName;
    String objectNameWeb;
    Long size;
    String lastModified;
    String type;
    boolean isHidden;

    @SneakyThrows
    public FileDTO(Result<Item> result) {
        var item = result.get();
        this.isDir = item.isDir();
        this.objectName = item.objectName();
        if (isDir) {
            this.objectNameWeb = objectName.substring(0, objectName.lastIndexOf('/'));
            this.objectNameWeb = objectNameWeb.substring(objectNameWeb.lastIndexOf('/') + 1);
        } else this.objectNameWeb = item.objectName().substring(item.objectName().lastIndexOf("/") + 1);
        this.size = item.size();
        if (!isDir) {
            this.lastModified = item.lastModified().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));
        } else lastModified = "";
        if (isDir) {
            type = "";
        } else type = objectNameWeb.substring(objectNameWeb.lastIndexOf('.') + 1);
        if (!isDir && objectName.endsWith("/")) {
            isHidden = true;
        }
    }

    public static List<FileDTO> getFileDTOList(Iterable<Result<Item>> itemList) {
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
