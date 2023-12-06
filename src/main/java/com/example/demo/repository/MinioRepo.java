package com.example.demo.repository;

import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.model.FileDTO;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Repository
public class MinioRepo {
    @Autowired
    MinioClient minioClient;


    public Iterable<Result<Item>> listPathObjects(String bucketName, String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .startAfter("")
                        .prefix(path)
                        .maxKeys(100)
                        .build());
    }

    public List<FileDTO> listPathObjectsDTO(String bucketName, String path) {
        return FileDTO.getFileDTOList(listPathObjects(bucketName, path));
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

    public Iterable<Result<Item>> searchFile(String bucketName, String name) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .startAfter("")
                        .prefix(name)
                        .maxKeys(100)
                        .build());
    }

    public void makeBucket(String name) throws S3StorageServerException {
        boolean found;
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
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException {
        try {
            for (MultipartFile file
                    : multipartFiles) {
                putObject(bucketName,
                        path + file.getOriginalFilename(),
                        file.getContentType(),
                        file.getInputStream());
            }
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) throws S3StorageServerException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                    inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public InputStream getObject(String bucketName, String objectName) throws S3StorageServerException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void removeObject(String bucketName, String objectName) throws S3StorageServerException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {

            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void createFolder(String bucketName, String folderName) throws S3StorageServerException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {

            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void copyObject(String bucketName, String objectName, String objectNameSource) throws S3StorageServerException, S3StorageFileNotFoundException {
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

        } catch (ErrorResponseException e) {
            throw new S3StorageFileNotFoundException("Copy Object :" + "File not found");
        } catch (ServerException | InternalException | XmlParserException | InvalidResponseException |
                 InvalidKeyException | NoSuchAlgorithmException | IOException | InsufficientDataException e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void renameObject(String bucketName, String fileName, String fileNameNew) throws S3StorageServerException, S3StorageFileNotFoundException {
        copyObject(bucketName, fileNameNew, fileName);
        removeObject(bucketName, fileName);
        log.info(fileName + " rename to " + fileNameNew);
    }

    public void removeListObjects(String bucketName, List<DeleteObject> objects) throws S3StorageServerException {
        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
        for (Result<DeleteError> result : results) {
            DeleteError error;
            try {
                error = result.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.warn(
                    "Error in deleting object " + error.objectName() + "; " + error.message());
            throw new S3StorageServerException("Error in deleting object " + error.objectName() + "; " + error.message());
        }
    }
}