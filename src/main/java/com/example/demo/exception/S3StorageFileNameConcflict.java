package com.example.demo.exception;

public class S3StorageFileNameConcflict extends Exception {
    public S3StorageFileNameConcflict(String message) {
        super(message);
    }
}
