package com.example.demo.service.s3Storage.Sync;

import com.example.demo.exception.S3StorageFileNameConcflict;
import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.repository.MinioRepo;
import com.example.demo.service.s3Storage.S3StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class S3StorageServiceWithSync extends S3StorageService {
    public S3StorageServiceWithSync(MinioRepo minioRepo) {
        super(minioRepo);
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNotFoundException {
        super.removeObject(bucketName, objectName);
    }

    @Override
    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageFileNameConcflict {
        super.putFolder(bucketName, multipartFiles, path);
    }

    @Override
    public void transferObject(String bucketName, String objectNameSource, String folderName) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        super.transferObject(bucketName, objectNameSource, folderName);
    }

    @Override
    public void renameObject(String bucketName, String fileName, String fileNameNew) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        super.renameObject(bucketName, fileName, fileNameNew);
    }

    @Override
    public void renameFolder(String bucketName, String folderName, String folderNameNew, String path) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        super.renameFolder(bucketName, folderName, folderNameNew, path);
    }

    @Override
    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException, S3StorageFileNameConcflict {
        super.putArrayObjects(bucketName, multipartFiles, path);
    }

    @Override
    public void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        super.putObject(bucketName, objectName, contentType, inputStream);
    }

    @Override
    public void deleteFolder(String bucketName, String folderName, String path) throws S3StorageServerException, S3StorageFileNotFoundException {
        super.deleteFolder(bucketName, folderName, path);
    }
}
