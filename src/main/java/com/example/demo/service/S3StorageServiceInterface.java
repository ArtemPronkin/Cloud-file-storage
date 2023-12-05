package com.example.demo.service;

import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.model.FileDTO;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public interface S3StorageServiceInterface {
    String generateStorageName(long id);

    ArrayList<FileDTO> searchFileDTO(String bucketName, String fileName) throws S3StorageServerException;

    Iterable<Result<Item>> listPathObjects(String bucketName, String path);

    List<FileDTO> listPathObjectsDTO(String bucketName, String path);

    Iterable<Result<Item>> listObjectsInFolder(String name, String foldername, String path);

    void makeBucket(String bucketName) throws S3StorageServerException;

    void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException;

    void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) throws S3StorageServerException;

    InputStream getObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException;

    void removeObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException;

    void createFolder(String bucketName, String folderName) throws S3StorageServerException;

    List<Result<Item>> findAllObjectInFolder(String bucketName, String folderName, String path) throws S3StorageServerException;

    void deleteFolder(String bucketName, String folderName, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException;

    void createFoldersForPath(String bucketName, String fullPathName) throws S3StorageServerException;

    void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException;

    void transferObject(String bucketName, String objectNameSource, String folderName) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException;

    void renameObject(String bucketName, String fileName, String fileNameNew) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException;

    void renameFolder(String bucketName, String folderName, String folderNameNew, String path) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException;
}
