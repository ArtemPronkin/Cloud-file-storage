package com.example.demo.Service;

import io.minio.*;
import io.minio.errors.*;
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

@Slf4j
@Component
public class MinioService {
    @Autowired
    MinioClient minioClient;

    public Iterable<Result<Item>> listObjects(String name) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(name).build());
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
    public InputStream  getObject(String bucketName, String objectName){
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        }catch (Exception e){
            log.info(e.getMessage());
            return null;
        }
    }
    @SneakyThrows
    public void putFolder(String bucketName, String path) {
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(path).stream(
                                new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    @SneakyThrows
    public void upload(String bucketName, String fileName, byte[] file) {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("asiatrip")
                        .object("asiaphotos-2015.zip")
                        .filename("/home/user/Photos/asiaphotos.zip")
                        .build());
    }

}


