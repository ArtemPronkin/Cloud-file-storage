package com.example.demo.service;

import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.model.FileDTO;
import com.example.demo.repository.MinioRepo;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
@Service
public class S3StorageService implements S3StorageServiceInterface {
    @Autowired
    MinioRepo minioRepo;

    public String generateStorageName(long id) {
        return "user-" + id + "-files";
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
        return minioRepo.listPathObjects(bucketName, path);
    }

    public List<FileDTO> listPathObjectsDTO(String bucketName, String path) {
        return minioRepo.listPathObjectsDTO(bucketName, path);
    }


    public Iterable<Result<Item>> listObjectsInFolder(String name, String foldername, String path) {
        return minioRepo.listObjectsInFolder(name, foldername, path);
    }


    public void makeBucket(String name) throws S3StorageServerException {
        minioRepo.makeBucket(name);
    }

    public void putArrayObjects(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        minioRepo.putArrayObjects(bucketName, multipartFiles, path);
    }

    public void putObject(String bucketName, String objectName, String contentType, InputStream inputStream) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        minioRepo.putObject(bucketName, objectName, contentType, inputStream);
    }

    public InputStream getObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        return minioRepo.getObject(bucketName, objectName);
    }


    public void removeObject(String bucketName, String objectName) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        minioRepo.removeObject(bucketName, objectName);
    }

    public void createFolder(String bucketName, String folderName) throws S3StorageServerException {
        minioRepo.createFolder(bucketName, folderName);
    }

    public List<Result<Item>> findAllObjectInFolder(String bucketName, String folderName, String path) throws S3StorageServerException {
        log.info(" ALLObjectFind file name :" + folderName + "  path  " + path);

        try {
            List<Result<Item>> result = new ArrayList<>();
            Iterable<Result<Item>> listFindObjects = listObjectsInFolder(bucketName, folderName, path);
            Queue<Iterable<Result<Item>>> queue = new LinkedList<>();
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

    public void deleteFolder(String bucketName, String folderName, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            var findList = findAllObjectInFolder(bucketName, folderName, path);
            for (Result<Item> itemResult : findList) {
                log.info("DeleteAllObjectInFolder :" + itemResult.get().objectName());
                objects.add(new DeleteObject(itemResult.get().objectName()));
            }
            minioRepo.removeListObjectc(bucketName, objects);
        } catch (
                Exception e) {
            throw new S3StorageServerException(e.getMessage());
        }

    }

    public void putFolder(String bucketName, MultipartFile[] multipartFiles, String path) throws S3StorageServerException, S3StorageResourseIsOccupiedException {
        Set<String> setPath = new HashSet<>();
        try {
            for (MultipartFile file :
                    multipartFiles) {

                String fullPathName = file.getOriginalFilename();
                log.info("PutFIle:  NameFile : " + fullPathName);
                assert fullPathName != null;
                setPath.add(path + fullPathName.substring(0, fullPathName.lastIndexOf('/') + 1));
                minioRepo.putObject(bucketName,
                        path + file.getOriginalFilename(),
                        file.getContentType(),
                        file.getInputStream());
            }
            var setFolderNames = giveSetFolderNamesFromSetPath(setPath);
            for (String folderName : setFolderNames) {
                minioRepo.createFolder(bucketName, folderName);
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

    public void transferObject(String bucketName, String objectNameSource, String folderName) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException {
        minioRepo.createFolder(bucketName, folderName);
        String nameFile = objectNameSource.substring(objectNameSource.lastIndexOf("/") + 1);
        var fullNewPathName = folderName + nameFile;
        createFoldersForPath(bucketName, fullNewPathName);
        minioRepo.renameObject(bucketName, objectNameSource, fullNewPathName);

        log.info(objectNameSource + " transfer to " + folderName + nameFile);
    }

    public void renameObject(String bucketName, String fileName, String fileNameNew) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException {
        minioRepo.renameObject(bucketName, fileName, fileNameNew);
    }


    public void renameFolder(String bucketName, String folderName, String folderNameNew, String path) throws S3StorageServerException, S3StorageFileNotFoundException, S3StorageResourseIsOccupiedException {
        try {
            if (!folderNameNew.endsWith("/")) {
                folderNameNew += "/";
            }
            var filesAndFolders = findAllObjectInFolder(bucketName, folderName, path);
            log.info("rename folder for " + folderName + " to " + folderNameNew + " path : " + path);
            StringBuilder folderNameNewBuilder = new StringBuilder(folderNameNew);
            if (filesAndFolders.isEmpty()) {
                throw new S3StorageFileNotFoundException("Rename Folder: Folder not found ");
            }
            createFoldersForPath(bucketName, folderNameNew);
            for (Result<Item> itemResult : filesAndFolders) {
                log.info(itemResult.get().objectName());
                var sourceName = itemResult.get().objectName();
                var nameNew = folderNameNewBuilder + sourceName.substring(folderName.length());
                if (!folderNameNewBuilder.toString().endsWith("/")) {
                    folderNameNewBuilder.append('/');
                }
                minioRepo.renameObject(bucketName, sourceName, nameNew);
                log.info("renameFolder : " + sourceName + " to " + nameNew);
            }
        } catch (ErrorResponseException e) {
            throw new S3StorageFileNotFoundException("Rename Folder: File not found ");
        } catch (ServerException | InternalException | XmlParserException | InvalidResponseException |
                 InvalidKeyException | NoSuchAlgorithmException | IOException | InsufficientDataException e) {
            throw new S3StorageServerException(e.getMessage());
        }
    }
}

