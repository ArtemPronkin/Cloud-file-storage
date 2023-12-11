package com.example.demo.service.s3Storage;

import com.example.demo.exception.S3StorageFileNameConcflict;
import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class S3StorageServiceSync extends S3StorageService {
    static ConcurrentHashMap<String, Boolean> bucketInfoMap = new ConcurrentHashMap<>();

    private static boolean bucketIsOccupied(String bucketName) {
        bucketInfoMap.putIfAbsent(bucketName, false);
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
    public InputStream getObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNotFoundException {
        occupiedBucket(bucketName);
        try {
            return super.getObject(bucketName, objectName);
        } finally {
            emptyBucket(bucketName);
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNotFoundException {
        occupiedBucket(bucketName);
        try {
            super.removeObject(bucketName, objectName);
        } finally {
            emptyBucket(bucketName);
        }

    }
    @Override
    public void deleteFolder(String bucketName, String folderName, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNotFoundException {
        occupiedBucket(bucketName);
        try {
            super.deleteFolder(bucketName, folderName, path);
        } finally {
            emptyBucket(bucketName);
        }

    }
    @Override
    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageFileNameConcflict {
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
