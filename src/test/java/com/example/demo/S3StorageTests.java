package com.example.demo;

import com.example.demo.Exception.S3StorageException;
import com.example.demo.Service.S3StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@Testcontainers
@SpringBootTest
public class S3StorageTests {
    //    @Container
//    @ServiceConnection
//    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql");
//    @Container
//    static MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
//            .withEnv("MINIO_ROOT_USER", "cloudStorage")
//            .withEnv("MINIO_ROOT_PASSWORD", "minio123")
//            .withCommand("server /data")
//            .withExposedPorts(9000);
//    @DynamicPropertySource
//    static void datasourceProperties(DynamicPropertyRegistry registry) {
//        registry.add("minio.url", container::getS3URL);
//        System.out.println(container.getS3URL());
//    }
    @Autowired
    S3StorageService s3StorageService;
    String bucketName;


    @BeforeEach
    void beforeEach() throws S3StorageException {
        bucketName = UUID.randomUUID().toString();
        s3StorageService.makeBucket(bucketName);
        System.out.println(bucketName);
    }

    @Test
    void storageSearchTest() throws S3StorageException {
        s3StorageService.createFolder(bucketName, "folder");
        Assertions.assertEquals(1, s3StorageService.searchFileDTO(bucketName, "folder").size());
        s3StorageService.createFolder(bucketName, "folder2");
        Assertions.assertEquals(2, s3StorageService.searchFileDTO(bucketName, "folder").size());
        s3StorageService.createFolder(bucketName, "folder/folder2");
        Assertions.assertEquals(3, s3StorageService.searchFileDTO(bucketName, "folder").size());
        s3StorageService.createFolder(bucketName, "folder/new");
        Assertions.assertEquals(3, s3StorageService.searchFileDTO(bucketName, "folder").size());
    }

    @Test
    void storageDeleteFolderTest() throws S3StorageException {
        s3StorageService.createFolder(bucketName, "test");
        s3StorageService.createFolder(bucketName, "test/2");
        s3StorageService.createFolder(bucketName, "test/2/3");
        s3StorageService.createFolder(bucketName, "test/2/3/4");
        s3StorageService.deleteFolder(bucketName, "test", "");
        Assertions.assertEquals(0, s3StorageService.searchFileDTO(bucketName, "test").size());
        Assertions.assertEquals(0, s3StorageService.searchFileDTO(bucketName, "2").size());
        Assertions.assertEquals(0, s3StorageService.searchFileDTO(bucketName, "3").size());
        Assertions.assertEquals(0, s3StorageService.searchFileDTO(bucketName, "4").size());
    }

    @Test
    void storageRenameFolderTest() throws S3StorageException {
        s3StorageService.createFolder(bucketName, "test");
        s3StorageService.createFolder(bucketName, "test/2");
        s3StorageService.createFolder(bucketName, "test/2/3");
        s3StorageService.createFolder(bucketName, "test/2/3/4/");
        s3StorageService.renameFolder(bucketName, "test/", "NewName/", "");
        Assertions.assertEquals(1, s3StorageService.searchFileDTO(bucketName, "NewName").size());
        Assertions.assertEquals("NewName/2/3/4/", s3StorageService.searchFileDTO(bucketName, "4").get(0).getObjectName());
        Assertions.assertEquals("NewName/2/3/", s3StorageService.searchFileDTO(bucketName, "3").get(0).getObjectName());
        Assertions.assertEquals("NewName/2/", s3StorageService.searchFileDTO(bucketName, "2").get(0).getObjectName());
    }

    @Test
    void storageCreateFoldersForPathTest() throws S3StorageException {
        s3StorageService.createFoldersForPath(bucketName, "test/2/3/4/5/6/7/test.exe");
        Assertions.assertEquals("test/2/3/4/5/", s3StorageService.searchFileDTO(bucketName, "5").get(0).getObjectName());


    }
}
