package com.example.demo.Service;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
public class MinioService {
    @Autowired
    MinioClient minioClient;

    public String generateStorageName(long id) {
        return "user-" + id + "-files";
    }

    @SneakyThrows
    public Iterable<Result<Item>> searchFile(String bucketName, String fileName) {
        ArrayList<Result<Item>> result = new ArrayList<>();
        Iterable<Result<Item>> allFile =
                minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucketName)
                                .recursive(true)
                                .build());
        for (Result<Item> itemResult : allFile) {
            if (itemResult.get().objectName()
                    .toLowerCase()
                    .contains(fileName.toLowerCase()) && !itemResult.get().isDir()) {
                result.add(itemResult);
            }
        }
        return result;

    }

    public Iterable<Result<Item>> listPathObjects(String bucketName, String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .startAfter("")
                        .prefix(path)
                        .maxKeys(100)
                        .build());
    }

    public Iterable<Result<Item>> listObjectsInFolder(String name, String foldername, String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(name)
                        .startAfter(path)
                        .prefix(foldername)
                        .maxKeys(100)
                        .build());
    }


    public void makeBucket(String name) {
        boolean found =
                false;
        try {
            found = minioClient.bucketExists(BucketExistsArgs
                    .builder()
                    .bucket(name)
                    .build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket(name)
                        .build());
            } else {
                log.info("Bucket " + name + " already exists.");
            }
        } catch (Exception e) {
            log.warn("makeBucket : " + e.getMessage());
        }
    }

    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) {
        try {
            for (MultipartFile file
                    : multipartFiles) {
                putObject(bucketName,
                        path + file.getOriginalFilename(),
                        file.getContentType(),
                        file.getInputStream());
            }
        } catch (Exception e) {
            log.warn("putArrayObjects : " + e.getMessage());
        }
    }

    public void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                    inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            log.warn("put Object : " + e.getMessage());
        }
    }

    public InputStream getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.warn("get Object : " + e.getMessage());
            return null;
        }
    }

    public void deleteObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.warn("deleteObject : " + e.getMessage());
        }
    }

    public void createFolder(String bucketName, String folderName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            log.warn("create Folder : " + e.getMessage());
        }
    }

    public Iterable<Result<Item>> findAllObjectInFolder(String bucketName, String folderName, String path) {
        try {
            List<Result<Item>> result = new ArrayList<>();
            Iterable<Result<Item>> listFindObjects = listObjectsInFolder(bucketName, folderName, path);
            Queue<Iterable<Result<Item>>> queue = new LinkedList();
            queue.add(listFindObjects);

            while (!queue.isEmpty()) {
                listFindObjects = queue.poll();

                for (Result<Item> cur :
                        listFindObjects) {
                    log.info("deleteFolder : " + cur.get().objectName());
                    if (cur.get().isDir()) {
                        var listNew = listObjectsInFolder(bucketName, cur.get().objectName(), cur.get().objectName());
                        queue.add(listNew);
                    }
                    result.add(cur);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("findAllObjectInFolder :" + e.getMessage());
        }
        return null;
    }

    public void deleteFolder(String bucketName, String folderName, String path) {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            var findList = findAllObjectInFolder(bucketName, folderName, path);
            for (Result<Item> itemResult : findList) {
                objects.add(new DeleteObject(itemResult.get().objectName()));
            }
            Iterable<Result<DeleteError>> results =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.warn(
                        "Error in deleting object " + error.objectName() + "; " + error.message());
            }
        } catch (
                Exception e) {
            log.warn("deleteFolder :" + e.getMessage());
        }

    }

    public void createFoldersForPath(String bucketName, String fullPathName) {
        var folderPath = "";
        var filename = fullPathName.substring(fullPathName.lastIndexOf("/") + 1);

        while (!fullPathName.equals(filename)) {

            var tk = new StringTokenizer(fullPathName, "/");
            folderPath = folderPath + tk.nextToken() + "/";
            fullPathName = fullPathName.substring(fullPathName.indexOf("/") + 1);

            createFolder(bucketName, folderPath);
        }
    }

    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) {
        try {
            for (MultipartFile file :
                    multipartFiles) {

                String fullPathName = file.getOriginalFilename();
                createFoldersForPath(bucketName, path + fullPathName);
                putObject(bucketName,
                        path + file.getOriginalFilename(),
                        file.getContentType(),
                        file.getInputStream());
            }
        } catch (Exception e) {
            log.warn("Put Folder : " + e.getMessage());
        }
    }

    public void copyObject(String bucketName, String objectName, String objectNameSource) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(objectNameSource)
                                            .build())
                            .build());
        } catch (Exception e) {
            log.warn("CopyObject " + e.getMessage());
        }
    }

    public void transferObject(String bucketName, String objectNameSource, String folderName) {
        createFolder(bucketName, folderName);
        String nameFile = objectNameSource.substring(objectNameSource.lastIndexOf("/") + 1);
        var fullNewPathName = folderName + nameFile;
        createFoldersForPath(bucketName, fullNewPathName);
        copyObject(bucketName, fullNewPathName, objectNameSource);
        deleteObject(bucketName, objectNameSource);

        log.info(objectNameSource + " transfer to " + folderName + nameFile);
    }

    public void renameObject(String bucketName, String fileName, String fileNameNew) {
        copyObject(bucketName, fileNameNew, fileName);
        deleteObject(bucketName, fileName);

        log.info(fileName + " rename to " + fileNameNew);
    }

    public void renameFolder(String bucketName, String folderName, String folderNameNew, String path) {
        try {
            var findList = findAllObjectInFolder(bucketName, folderName, "");
            for (Result<Item> itemResult : findList) {
                var sourceName = itemResult.get().objectName();
                var nameNew = folderNameNew + sourceName.substring(folderName.length());
                createFoldersForPath(bucketName, folderNameNew);
                renameObject(bucketName, itemResult.get().objectName(), nameNew);
            }
        } catch (Exception e) {
            log.warn("renameFolder : " + e.getMessage());
        }
    }
}


