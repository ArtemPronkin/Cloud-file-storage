package com.example.demo;

import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.service.S3StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
public class S3StorageTests {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql");
    @Container
    static MinIOContainer minIOContainer = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withEnv("MINIO_ROOT_USER", "cloudStorage")
            .withEnv("MINIO_ROOT_PASSWORD", "minio123")
            .withCommand("server /data")
            .withExposedPorts(9000);
    @Autowired
    S3StorageService s3StorageService;
    String bucketName;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minIOContainer::getS3URL);
    }

    @BeforeEach
    void beforeEach() throws S3StorageServerException {
        bucketName = UUID.randomUUID().toString();
        s3StorageService.makeBucket(bucketName);
    }

    @Test
    void Must_MySQLContainerIsRun_WhenTestsStarted() {
        Assertions.assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void Must_minIOContainerIsRun_WhenTestsStarted() {
        Assertions.assertTrue(minIOContainer.isRunning());
    }

    @Test
    void Must_NewFolderFound_WhenNewFolderIsCreated() throws S3StorageServerException {
        s3StorageService.createFolder(bucketName, "folder");
        assertEquals(1, s3StorageService.searchFileDTO(bucketName, "folder").size());

        s3StorageService.createFolder(bucketName, "folder2");
        assertEquals(2, s3StorageService.searchFileDTO(bucketName, "folder").size());

        s3StorageService.createFolder(bucketName, "folder/folder2");
        assertEquals(3, s3StorageService.searchFileDTO(bucketName, "folder").size());

        s3StorageService.createFolder(bucketName, "folder/new");
        assertEquals(3, s3StorageService.searchFileDTO(bucketName, "folder").size());
    }

    @Test
    void Must_ChildFolderRemoved_WhenParentFolderRemoved() throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        s3StorageService.createFolder(bucketName, "test");
        s3StorageService.createFolder(bucketName, "test/2");
        s3StorageService.createFolder(bucketName, "test/2/3");
        s3StorageService.createFolder(bucketName, "test/2/3/4");


        assertEquals("test/2/3/4/", s3StorageService.searchFileDTO(bucketName, "4").get(0).getObjectName());
        assertEquals("test/2/3/", s3StorageService.searchFileDTO(bucketName, "3").get(0).getObjectName());
        assertEquals("test/2/", s3StorageService.searchFileDTO(bucketName, "2").get(0).getObjectName());

        s3StorageService.deleteFolder(bucketName, "test/", "");

        assertEquals(0, s3StorageService.searchFileDTO(bucketName, "test").size());
        assertEquals(0, s3StorageService.searchFileDTO(bucketName, "2").size());
        assertEquals(0, s3StorageService.searchFileDTO(bucketName, "3").size());
        assertEquals(0, s3StorageService.searchFileDTO(bucketName, "4").size());

    }

    @Test
    void Must_ChildFolderRename_WhenParentFolderRename() throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException {
        s3StorageService.createFolder(bucketName, "test");
        s3StorageService.createFolder(bucketName, "test/2");
        s3StorageService.createFolder(bucketName, "test/2/3");
        s3StorageService.createFolder(bucketName, "test/2/3/4/");

        s3StorageService.renameFolder(bucketName, "test/", "NewName/", "");

        assertEquals("NewName/2/3/4/", s3StorageService.searchFileDTO(bucketName, "4").get(0).getObjectName());
        assertEquals("NewName/2/3/", s3StorageService.searchFileDTO(bucketName, "3").get(0).getObjectName());
        assertEquals("NewName/2/", s3StorageService.searchFileDTO(bucketName, "2").get(0).getObjectName());
    }

    @Test
    void Must_ParentsFolderIsCreated_WhenCreatedChildFolder() throws S3StorageServerException {
        s3StorageService.createFoldersForPath(bucketName, "test/2/3/4/5/6/7/");
        assertEquals("test/2/3/4/5/", s3StorageService.searchFileDTO(bucketName, "5").get(0).getObjectName());
    }

    @Test
    void Must_NewFileFound_WhenNewFileIsUploaded() throws S3StorageServerException, IOException {
        var name = "hello.txt";
        var file = getMultiPartFile(name);

        s3StorageService.putObject(bucketName, file.getOriginalFilename(), file.getContentType(), file.getInputStream());

        assertEquals(file.getOriginalFilename(),
                s3StorageService.searchFileDTO(bucketName, "hello.txt").get(0).getObjectName());

        name = "folder/folder/hello2.txt";
        file = getMultiPartFile(name);

        s3StorageService.putObject(bucketName, file.getOriginalFilename(), file.getContentType(), file.getInputStream());

        assertEquals(file.getOriginalFilename(),
                s3StorageService.searchFileDTO(bucketName, "hello2.txt").get(0).getObjectName());
    }

    @Test
    void Must_FileAndSubFolderFound_WhenFolderWithFileUploaded() throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        var file1 = getMultiPartFile("folder1/folder2/folder3/folder4/hello.txt");
        var file2 = getMultiPartFile("folder1/folder2/folder3/folder4/hello2.txt");
        var file3 = getMultiPartFile("folder1/folder2/folder3/folder4/hello3.txt");
        var files = new MultipartFile[]{file1, file2, file3};
        var path = "path/";

        s3StorageService.putFolder(bucketName, files, path);
        assertEquals(path + file1.getOriginalFilename(),
                s3StorageService.searchFileDTO(bucketName, "hello.txt").get(0).getObjectName());
        assertEquals(path + file2.getOriginalFilename(),
                s3StorageService.searchFileDTO(bucketName, "hello2.txt").get(0).getObjectName());
        assertEquals(path + file3.getOriginalFilename(),
                s3StorageService.searchFileDTO(bucketName, "hello3.txt").get(0).getObjectName());
        assertEquals(path + "folder1/folder2/folder3/folder4/",
                s3StorageService.searchFileDTO(bucketName, "folder4").get(0).getObjectName());
        assertEquals(path + "folder1/folder2/folder3/",
                s3StorageService.searchFileDTO(bucketName, "folder3").get(0).getObjectName());
        assertEquals(path + "folder1/folder2/",
                s3StorageService.searchFileDTO(bucketName, "folder2").get(0).getObjectName());
        assertEquals(path + "folder1/",
                s3StorageService.searchFileDTO(bucketName, "folder1").get(0).getObjectName());

        assertTrue(s3StorageService.findAllObjectInFolder(bucketName, "", "").size() > 0);
        s3StorageService.deleteFolder(bucketName, "path/", "");
        assertEquals(0, s3StorageService.findAllObjectInFolder(bucketName, "", "").size());
    }

    @Test
    void Must_FilesAndFoldersNotFound_WhenParentFolderWithFileRemoved() throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        var file1 = getMultiPartFile("folder1/folder2/folder3/folder4/hello.txt");
        var file2 = getMultiPartFile("folder1/folder2/folder3/folder4/hello2.txt");
        var file3 = getMultiPartFile("folder1/folder2/folder3/folder4/hello3.txt");
        var files = new MultipartFile[]{file1, file2, file3};
        var path = "path/";
        s3StorageService.putFolder(bucketName, files, path);

        assertTrue(s3StorageService.findAllObjectInFolder(bucketName, "", "").size() > 0);
        s3StorageService.deleteFolder(bucketName, "path/", "");
        assertEquals(0, s3StorageService.findAllObjectInFolder(bucketName, "", "").size());
    }

    MultipartFile getMultiPartFile(String name) {

        return new MockMultipartFile(
                "file",
                name,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
    }
}
