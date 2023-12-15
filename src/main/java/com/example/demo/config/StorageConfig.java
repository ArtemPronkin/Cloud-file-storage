package com.example.demo.config;

import com.example.demo.service.s3Storage.S3StorageServiceInterface;
import com.example.demo.service.s3Storage.S3StorageServiceWithSync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
    @Bean
    public S3StorageServiceInterface workStorageService() {
        return new S3StorageServiceWithSync();

    }
}
