package com.example.demo.controller;

import com.example.demo.exception.S3StorageFileNameConcflict;
import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.model.FileDTO;
import com.example.demo.service.S3StorageServiceSync;
import com.example.demo.service.security.MyPrincipal;
import com.example.demo.util.PathNameUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "/storage")
public class FileStorage {
    @Autowired
    S3StorageServiceSync s3StorageService;
    @Autowired
    PathNameUtils pathNameUtils;

    @GetMapping
    String storage(@AuthenticationPrincipal MyPrincipal user, Model model,
                   @RequestParam Optional<String> path,
                   @RequestParam Optional<String> sort,
                   @RequestParam Optional<String> search)
            throws
            S3StorageServerException {

        List<FileDTO> listDTO;
        if (search.isPresent() && !search.get().isEmpty()) {
            listDTO
                    = s3StorageService.searchFileDTO
                    (s3StorageService.generateStorageName(user.getId()), search.get());
        } else {
            listDTO
                    = s3StorageService.listPathObjectsDTO
                    (s3StorageService.generateStorageName(user.getId()), path.orElse(""));
        }
        listDTO = sort(listDTO, sort);
        model.addAttribute("objectList", listDTO
        );
        model.addAttribute("path", path.orElse(""));
        model.addAttribute("search", search.orElse(""));
        model.addAttribute("backPath", pathNameUtils.getBackPath(path.orElse("")));
        return "storage";
    }

    @GetMapping(value = "/download")
    void getFile(@AuthenticationPrincipal MyPrincipal user,
                 @RequestParam String fileName, HttpServletResponse response)
            throws
            S3StorageServerException,
            S3StorageResourseIsOccupiedException,
            S3StorageFileNotFoundException {

        var file = s3StorageService.getObject(s3StorageService.generateStorageName(user.getId()), fileName);
        var downloadName = pathNameUtils.encode(fileName.substring(fileName.lastIndexOf("/") + 1));
        response.setHeader("Content-Disposition", "attachment; filename=" + downloadName);
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            FileCopyUtils.copy(file, response.getOutputStream());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @PostMapping("/putFile")
    String putFile(@AuthenticationPrincipal MyPrincipal user,
                   @RequestParam("file") MultipartFile[] files,
                   @RequestParam Optional<String> path)
            throws
            S3StorageServerException,
            S3StorageResourseIsOccupiedException,
            S3StorageFileNameConcflict {

        log.info("put on " + path.orElse("/"));
        s3StorageService.putArrayObjects
                (s3StorageService.generateStorageName(user.getId()), files, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/putFolder")
    String putFolder(@AuthenticationPrincipal MyPrincipal user,
                     @RequestParam("file") MultipartFile[] files,
                     @RequestParam Optional<String> path)
            throws
            S3StorageServerException,
            S3StorageResourseIsOccupiedException,
            S3StorageFileNameConcflict {

        log.info("put on " + path.orElse("/"));
        s3StorageService.putFolder(s3StorageService.generateStorageName(user.getId()), files, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @DeleteMapping(value = "/delete")
    String deleteFile(@AuthenticationPrincipal MyPrincipal user,
                      @RequestParam String fileName,
                      @RequestParam Optional<String> path)
            throws
            S3StorageServerException,
            S3StorageResourseIsOccupiedException {

        s3StorageService.removeObject(s3StorageService.generateStorageName(user.getId()), fileName);
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @DeleteMapping(value = "/deleteFolder")
    String deleteAllFileInFolder(@AuthenticationPrincipal MyPrincipal user,
                                 @RequestParam("folderName") String folderName,
                                 @RequestParam Optional<String> path)
            throws
            S3StorageServerException,
            S3StorageResourseIsOccupiedException {

        s3StorageService.deleteFolder
                (s3StorageService.generateStorageName(user.getId()), folderName, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/createFolder")
    String createFolder(@AuthenticationPrincipal MyPrincipal user,
                        @RequestParam("folderName") Optional<String> folderName,
                        @RequestParam Optional<String> path)
            throws
            S3StorageServerException {

        if (folderName.isPresent() && !folderName.get().isBlank()) {
            s3StorageService.createFoldersForPath
                    (s3StorageService.generateStorageName(user.getId()),
                            path.orElse("") + folderName.orElse("") + "/");
        }
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PatchMapping("/transferFile")
    String transferFile(@AuthenticationPrincipal MyPrincipal user,
                        @RequestParam String fileName,
                        @RequestParam Optional<String> path,
                        @RequestParam Optional<String> folderName)
            throws
            S3StorageServerException,
            S3StorageFileNotFoundException,
            S3StorageResourseIsOccupiedException,
            S3StorageFileNameConcflict {
        s3StorageService.transferObject
                (s3StorageService.generateStorageName(user.getId()), fileName, folderName.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PatchMapping("/renameFile")
    String renameFile(@AuthenticationPrincipal MyPrincipal user,
                      @RequestParam String fileName,
                      @RequestParam String fileNameNew,
                      @RequestParam Optional<String> path)
            throws
            S3StorageServerException,
            S3StorageFileNotFoundException,
            S3StorageResourseIsOccupiedException,
            S3StorageFileNameConcflict {
        s3StorageService.renameObject(s3StorageService.generateStorageName(user.getId()),
                fileName, path.orElse("") + fileNameNew);
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PatchMapping("/renameFolder")
    String renameFolder(@AuthenticationPrincipal MyPrincipal user,
                        @RequestParam String folderName,
                        @RequestParam String folderNameNew,
                        @RequestParam Optional<String> path)
            throws
            S3StorageServerException,
            S3StorageFileNotFoundException,
            S3StorageResourseIsOccupiedException {
        s3StorageService.renameFolder
                (s3StorageService.generateStorageName(user.getId()),
                        folderName, folderNameNew, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    private List<FileDTO> sort(List<FileDTO> listDTO, Optional<String> sort) {
        if (sort.isEmpty() || sort.get().equals("name")) {
            listDTO
                    = listDTO
                    .stream().sorted(Comparator.comparing(p -> p.getObjectName().toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (sort.isPresent() && sort.get().equals("size")) {
            listDTO
                    = listDTO
                    .stream().sorted(Comparator.comparing(FileDTO::getSize))
                    .collect(Collectors.toList());
        }
        if (sort.isPresent() && sort.get().equals("date")) {
            listDTO
                    = listDTO
                    .stream().sorted(Comparator.comparing(FileDTO::getLastModified))
                    .collect(Collectors.toList());
        }
        if (sort.isPresent() && sort.get().equals("type")) {
            listDTO
                    = listDTO
                    .stream().sorted(Comparator.comparing(FileDTO::getType))
                    .collect(Collectors.toList());
        }

        return listDTO;
    }

}
