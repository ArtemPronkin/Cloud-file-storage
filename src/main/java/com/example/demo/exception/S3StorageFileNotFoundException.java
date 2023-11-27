package com.example.demo.exception;

public class S3StorageFileNotFoundException extends Exception {
    public S3StorageFileNotFoundException(String message) {
        super(message);
    }
}
