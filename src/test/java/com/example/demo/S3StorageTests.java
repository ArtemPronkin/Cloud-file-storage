package com.example.demo;

import com.example.demo.Service.S3StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class S3StorageTests {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql");
    @Container
    static MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withEnv("MINIO_ROOT_USER", "cloudStorage")
            .withEnv("MINIO_ROOT_PASSWORD", "minio123")
            .withCommand("server /data")
            .withExposedPorts(9000);
    @Autowired
    S3StorageService s3StorageService;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", container::getS3URL);
    }

    @Test
    void storageTest() throws Exception {

        s3StorageService.makeBucket("test");
        s3StorageService.createFolder("test", "folder");
        Assertions.assertEquals(1, s3StorageService.searchFileDTO("test", "folder").size());
    }
}
