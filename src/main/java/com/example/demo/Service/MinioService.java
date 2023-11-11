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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

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
    public Iterable<Result<Item>> listPathObjects(String name,String path) {
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
            log.info(e.getMessage());
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
            log.info(e.getMessage());
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
            log.info(e.getMessage());
            return null;
        }
    }

    public void deleteObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void createFolder(String bucketName, String folderName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void deleteFolder(String bucketName, String folderName){
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
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
}


