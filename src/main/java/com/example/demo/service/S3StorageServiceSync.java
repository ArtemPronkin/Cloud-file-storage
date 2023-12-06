package com.example.demo.service;

import com.example.demo.exception.S3StorageFileNameConcflict;
import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.model.FileDTO;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class S3StorageServiceSync extends S3StorageService {
    static ConcurrentHashMap<String, Boolean> bucketInfoMap = new ConcurrentHashMap<>();

    private static boolean bucketIsOccupied(String bucketName) {
        if (!bucketInfoMap.containsKey(bucketName)) {
            bucketInfoMap.putIfAbsent(bucketName, false);
            return false;
        }
        return bucketInfoMap.get(bucketName);
    }

    private static void takeBucket(String bucketName) {
        bucketInfoMap.put(bucketName, true);
    }

    private static void emptyBucket(String bucketName) {
        bucketInfoMap.put(bucketName, false);
    }

    private static void occupiedBucket(String bucketName) throws S3StorageResourseIsOccupiedException {
        if (bucketIsOccupied(bucketName)) {
            log.info("S3StorageServiceSync : Resourse Is Occupied");
            throw new S3StorageResourseIsOccupiedException("Resourse Is Occupied");
        } else takeBucket(bucketName);
    }

    @Override
    public String generateStorageName(long id) {
        return super.generateStorageName(id);
    }

    @Override
    public ArrayList<FileDTO> searchFileDTO(String bucketName, String fileName) throws S3StorageServerException {
        return super.searchFileDTO(bucketName, fileName);
    }

    @Override
    public Iterable<Result<Item>> listPathObjects(String bucketName, String path) {
        return super.listPathObjects(bucketName, path);
    }

    @Override
    public List<FileDTO> listPathObjectsDTO(String bucketName, String path) {
        return super.listPathObjectsDTO(bucketName, path);
    }

    @Override
    public Iterable<Result<Item>> listObjectsInFolder(String name, String folderName, String path) {
        return super.listObjectsInFolder(name, folderName, path);
    }

    @Override
    public void makeBucket(String bucketName) throws S3StorageServerException {
        super.makeBucket(bucketName);
    }

    @Override
    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        occupiedBucket(bucketName);
        try {
            super.putArrayObjects(bucketName, multipartFiles, path);
        } finally {
            emptyBucket(bucketName);
        }
    }

    @Override
    public void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        occupiedBucket(bucketName);
        try {
            super.putObject(bucketName, objectName, contentType, inputStream);
        } finally {
            emptyBucket(bucketName);
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        occupiedBucket(bucketName);
        try {
            return super.getObject(bucketName, objectName);
        } finally {
            emptyBucket(bucketName);
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        occupiedBucket(bucketName);
        try {
            super.removeObject(bucketName, objectName);
        } finally {
            emptyBucket(bucketName);
        }

    }

    @Override
    public void createFolder(String bucketName, String folderName) throws S3StorageServerException {
        super.createFolder(bucketName, folderName);
    }

    @Override
    public List<Result<Item>> findAllObjectInFolder(String bucketName, String folderName, String path) throws S3StorageServerException {
        return super.findAllObjectInFolder(bucketName, folderName, path);
    }

    @Override
    public void deleteFolder(String bucketName, String folderName, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        occupiedBucket(bucketName);
        try {
            super.deleteFolder(bucketName, folderName, path);
        } finally {
            emptyBucket(bucketName);
        }

    }

    @Override
    public void createFoldersForPath(String bucketName, String fullPathName) throws S3StorageServerException {
        super.createFoldersForPath(bucketName, fullPathName);
    }

    @Override
    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        super.putFolder(bucketName, multipartFiles, path);
    }

    @Override
    public void transferObject(String bucketName, String objectNameSource, String folderName) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        occupiedBucket(bucketName);
        try {
            super.transferObject(bucketName, objectNameSource, folderName);
        } finally {
            emptyBucket(bucketName);
        }
    }

    @Override
    public void renameObject(String bucketName, String fileName, String fileNameNew) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        occupiedBucket(bucketName);
        try {
            super.renameObject(bucketName, fileName, fileNameNew);
        } finally {
            emptyBucket(bucketName);
        }
    }

    @Override
    public void renameFolder(String bucketName, String folderName, String folderNameNew, String path) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException {
        occupiedBucket(bucketName);
        try {
            super.renameFolder(bucketName, folderName, folderNameNew, path);
        } catch (S3StorageFileNameConcflict e) {
            throw new RuntimeException(e);
        } finally {
            emptyBucket(bucketName);
        }
    }
}
