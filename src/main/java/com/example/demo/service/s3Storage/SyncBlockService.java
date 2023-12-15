package com.example.demo.service.s3Storage;

import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SyncBlockService {
    protected static ConcurrentHashMap<String, Boolean> bucketInfoMap = new ConcurrentHashMap<>();

    protected static boolean bucketIsOccupied(String bucketName) {
        bucketInfoMap.putIfAbsent(bucketName, false);
        return bucketInfoMap.get(bucketName);
    }

    protected static void takeBucket(String bucketName) {
        bucketInfoMap.put(bucketName, true);
    }

    protected static void emptyBucket(String bucketName) {
        bucketInfoMap.put(bucketName, false);
    }

    protected static void occupiedBucket(String bucketName) throws S3StorageResourseIsOccupiedException {
        if (bucketIsOccupied(bucketName)) {
            log.info("S3StorageServiceSync : Resourse Is Occupied");
            throw new S3StorageResourseIsOccupiedException("Resourse Is Occupied");
        } else takeBucket(bucketName);
    }
}