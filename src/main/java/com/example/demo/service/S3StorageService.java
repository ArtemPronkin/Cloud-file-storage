package com.example.demo.service;

import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.model.FileDTO;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Component
public class S3StorageService {
    @Autowired
    MinioClient minioClient;

    public String generateStorageName(long id) {
        return "user-" + id + "-files";
    }

    @SneakyThrows
    public Iterable<Result<Item>> listAllFile(String bucketName, String fileName) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .startAfter(fileName)
                        .prefix("")
                        .recursive(true)
                        .maxKeys(100)
                        .build());

    }

    public ArrayList<FileDTO> searchFileDTO(String bucketName, String fileName) throws S3StorageServerException {
        var allFiles = FileDTO.getFileDTOList(findAllObjectInFolder(bucketName, "", ""));
        ArrayList<FileDTO> result = new ArrayList<>();
        for (FileDTO fileDTO : allFiles) {
            log.info(fileDTO.getObjectName());
            if (fileDTO.getObjectNameWeb().contains(fileName)) {
                result.add(fileDTO);
            }
        }
        return result;

    }

    public Iterable<Result<Item>> listPathObjects(String bucketName, String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .startAfter("")
                        .prefix(path)
                        .maxKeys(100)
                        .build());
    }

    public List<FileDTO> listPathObjectsDTO(String bucketName, String path) {
        return FileDTO.getFileDTOList(listPathObjects(bucketName, path));
    }


    public Iterable<Result<Item>> listObjectsInFolder(String name, String foldername, String path) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(name)
                        .startAfter(path)
                        .prefix(foldername)
                        .maxKeys(100)
                        .build());
    }


    public void makeBucket(String name) throws S3StorageServerException {
        boolean found =
                false;
        try {
            found = minioClient.bucketExists(BucketExistsArgs
                    .builder()
                    .bucket(name)
                    .build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs
                        .builder()
                        .bucket(name)
                        .build());
            } else {
                log.info("Bucket " + name + " already exists.");
            }
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException {
        try {
            for (MultipartFile file
                    : multipartFiles) {
                putObject(bucketName,
                        path + file.getOriginalFilename(),
                        file.getContentType(),
                        file.getInputStream());
            }
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) throws S3StorageServerException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                    inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public InputStream getObject(String bucketName, String objectName) throws S3StorageServerException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void deleteObject(String bucketName, String objectName) throws S3StorageServerException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {

            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void createFolder(String bucketName, String folderName) throws S3StorageServerException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {

            throw new S3StorageServerException(e.getMessage());
        }
    }

    public List<Result<Item>> findAllObjectInFolder(String bucketName, String folderName, String path) throws S3StorageServerException {
        log.info(" ALLObjectFind file name :" + folderName + "  path  " + path);

        try {
            List<Result<Item>> result = new ArrayList<>();
            Iterable<Result<Item>> listFindObjects = listObjectsInFolder(bucketName, folderName, path);
            Queue<Iterable<Result<Item>>> queue = new LinkedList();
            queue.add(listFindObjects);

            while (!queue.isEmpty()) {
                listFindObjects = queue.poll();

                for (Result<Item> cur :
                        listFindObjects) {
                    if (cur.get().isDir()) {
                        var listNew = listObjectsInFolder(bucketName, cur.get().objectName(), cur.get().objectName());
                        queue.add(listNew);
                    }
                    result.add(cur);
                }
            }
            return result;
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void deleteFolder(String bucketName, String folderName, String path) throws S3StorageServerException {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            var findList = findAllObjectInFolder(bucketName, folderName, path);
            for (Result<Item> itemResult : findList) {
                log.info("DeleteAllObjectInFolder :" + itemResult.get().objectName());
                objects.add(new DeleteObject(itemResult.get().objectName()));
            }
            Iterable<Result<DeleteError>> results =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.warn(
                        "Error in deleting object " + error.objectName() + "; " + error.message());
                throw new S3StorageServerException("Error in deleting object " + error.objectName() + "; " + error.message());
            }
        } catch (
                Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }

    }

    public void createFoldersForPath(String bucketName, String fullPathName) throws S3StorageServerException {
        var folderPath = "";
        var folderPathArray = fullPathName.split("/");
        log.info("createFoldersForPath Array Lenth : " + folderPathArray.length);
        var length = folderPathArray.length;
        if (!fullPathName.endsWith("/")) {
            length--;
        }
        for (int i = 0; i < length; i++) {
            folderPath = folderPath + folderPathArray[i] + "/";
            createFolder(bucketName, folderPath);
            log.info("createFoldersForPath create folder : " + folderPath);
        }
    }

    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException {
        Set<String> setPath = new HashSet<>();
        try {
            for (MultipartFile file :
                    multipartFiles) {

                String fullPathName = file.getOriginalFilename();
                log.info("PutFIle:  NameFile : " + fullPathName);
                setPath.add(path + fullPathName.substring(0, fullPathName.lastIndexOf('/') + 1));
                putObject(bucketName,
                        path + file.getOriginalFilename(),
                        file.getContentType(),
                        file.getInputStream());
            }
            var setFolderNames = giveSetFolderNamesFromSetPath(setPath);
            for (String folderName : setFolderNames) {
                createFolder(bucketName, folderName);
                log.info("Create unique folder: " + folderName);
            }
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    private Set<String> giveSetFolderNamesFromSetPath(Set<String> setPath) {
        Set<String> setFolderNames = new HashSet<>();
        for (String uniquePath : setPath) {
            var folderPath = "";
            var folderPathArray = uniquePath.split("/");
            log.info("UniquePath Array Lenth : " + folderPathArray.length);
            var length = folderPathArray.length;
            if (!uniquePath.endsWith("/")) {
                length--;
            }
            for (int i = 0; i < length; i++) {
                folderPath = folderPath + folderPathArray[i] + "/";
                setFolderNames.add(folderPath);
                log.info("UniquePath : " + folderPath);
            }
        }
        return setFolderNames;
    }

    public void copyObject(String bucketName, String objectName, String objectNameSource) throws S3StorageServerException, S3StorageFileNotFoundException {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(objectNameSource)
                                            .build())
                            .build());

        } catch (ErrorResponseException e) {
            throw new S3StorageFileNotFoundException("File not found");
        } catch (ServerException | InternalException | XmlParserException | InvalidResponseException |
                 InvalidKeyException | NoSuchAlgorithmException | IOException | InsufficientDataException e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }

    public void transferObject(String bucketName, String objectNameSource, String folderName) throws S3StorageServerException, S3StorageFileNotFoundException {
        createFolder(bucketName, folderName);
        String nameFile = objectNameSource.substring(objectNameSource.lastIndexOf("/") + 1);
        var fullNewPathName = folderName + nameFile;
        createFoldersForPath(bucketName, fullNewPathName);
        copyObject(bucketName, fullNewPathName, objectNameSource);
        deleteObject(bucketName, objectNameSource);

        log.info(objectNameSource + " transfer to " + folderName + nameFile);
    }

    public void renameObject(String bucketName, String fileName, String fileNameNew) throws S3StorageServerException, S3StorageFileNotFoundException {
        copyObject(bucketName, fileNameNew, fileName);
        deleteObject(bucketName, fileName);
        log.info(fileName + " rename to " + fileNameNew);
    }

    @SneakyThrows
    public void renameFolder(String bucketName, String folderName, String folderNameNew, String path) {
        try {
            var findList = findAllObjectInFolder(bucketName, folderName, path);
            log.info("rename folder for " + folderName + " to " + folderNameNew + " path : " + path);
            StringBuilder folderNameNewBuilder = new StringBuilder(folderNameNew);
            for (Result<Item> itemResult : findList) {
                log.info(itemResult.get().objectName());
                var sourceName = itemResult.get().objectName();
                var nameNew = folderNameNewBuilder + sourceName.substring(folderName.length());
                if (!folderNameNewBuilder.toString().endsWith("/")) {
                    folderNameNewBuilder.append('/');
                }
                renameObject(bucketName, sourceName, nameNew);
                log.info("renameFolder : " + sourceName + " to " + nameNew);
            }
        } catch (Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }
}


