package com.example.demo.Service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MinioService {
    @Autowired
    MinioClient minioClient;

    public String generateStorageName(long id) {
        return "user-" + id + "-files";
    }

    public Iterable<Result<Item>> listObjects(String name) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(name).build());
        return results;
    }

    public Iterable<Result<Item>> listPathObjects(String name, String path) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(name)
                        .startAfter("")
                        .prefix(path)
                        .maxKeys(100)
                        .build());
        return results;
    }


    public void makeBucket(String name) {
        boolean found =
                false;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            } else {
                log.info("Bucket " + name + " already exists.");
            }
        } catch (Exception e) {
            log.info("makeBucket : " + e.getMessage());
        }
    }

    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) {
        try {
            for (MultipartFile file :
                    multipartFiles) {
                putObject(bucketName, path + file.getOriginalFilename(), file.getContentType(), file.getInputStream());
            }
        } catch (Exception e) {
            log.info("putArrayObjects : " + e.getMessage());
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
            log.info("put Object : " + e.getMessage());
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
            log.info("get Object : " + e.getMessage());
            return null;
        }
    }

    public void deleteObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.info("deleteObject : " + e.getMessage());
        }
    }

    public void createFolder(String bucketName, String folderName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            log.info("create Folder : " + e.getMessage());
        }
    }

    public void deleteFolder(String bucketName, String folderName) {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            Iterable<Result<Item>> list = listPathObjects(bucketName, folderName);
            for (Result<Item> cur :
                    list) {
                objects.add(new DeleteObject(cur.get().objectName()));
            }

            Iterable<Result<DeleteError>> results =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.info(
                        "Error in deleting object " + error.objectName() + "; " + error.message());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) {
        try {
            for (MultipartFile file :
                    multipartFiles) {
                String fullPathName = file.getOriginalFilename();
                var folderPath = "";
                var filename = fullPathName.substring(fullPathName.lastIndexOf("/")+1);
                while (!fullPathName.equals(filename)) {
                    var tk = new StringTokenizer(fullPathName, "/");
                    folderPath = folderPath + tk.nextToken() + "/";
                    System.out.println(folderPath);
                    fullPathName = fullPathName.substring(fullPathName.indexOf("/")+1);
                    createFolder(bucketName,folderPath);
                }
                putObject(bucketName, path + file.getOriginalFilename(), file.getContentType(), file.getInputStream());
            }
        } catch (Exception e) {
            log.info("Put Folder : " + e.getMessage());
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
            log.info("CopyObject " + e.getMessage());
        }
    }

    public void transferObject(String bucketName, String objectNameSource, String folderName) {
        createFolder(bucketName, folderName);
        String nameFile = objectNameSource.substring(objectNameSource.lastIndexOf("/") + 1);
        log.info(objectNameSource + " transfer to " + folderName + nameFile);
        copyObject(bucketName, folderName + nameFile, objectNameSource);
        deleteObject(bucketName, objectNameSource);
    }
}


