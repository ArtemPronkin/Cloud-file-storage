package com.example.demo.сonfig;

import com.example.demo.service.S3StorageServiceInterface;
import com.example.demo.service.S3StorageServiceSync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {
    @Bean
    public S3StorageServiceInterface workStorageService() {
        return new S3StorageServiceSync();

    }
}
