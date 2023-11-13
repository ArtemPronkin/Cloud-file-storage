package com.example.demo.Controller;

import com.example.demo.Service.MinioService;
import com.example.demo.Service.MyPrincipal;
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
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/storage")
public class FileStorage {
    @Autowired
    MinioService minioService;
    @Autowired
    PathNameUtils pathNameUtils;

    @GetMapping
    String storage(@AuthenticationPrincipal MyPrincipal user, Model model, @RequestParam Optional<String> path) {
        var list = minioService.listPathObjects(minioService.generateStorageName(user.getId()), path.orElse(""));
        model.addAttribute("objectList", list);
        model.addAttribute("path", path.orElse(""));
        model.addAttribute("backPath", pathNameUtils.getBackPath(path.orElse("/")));
        return "storage";
    }

    @GetMapping(value = "/download")
    void getFile(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName, HttpServletResponse response) {

        var file = minioService.getObject(minioService.generateStorageName(user.getId()), fileName);
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
    String putFile(@AuthenticationPrincipal MyPrincipal user, @RequestParam("file") MultipartFile[] files, @RequestParam Optional<String> path) {
        log.info("put on " + path.orElse("/"));
        minioService.putArrayObjects(minioService.generateStorageName(user.getId()), files, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/putFolder")
    String putFolder(@AuthenticationPrincipal MyPrincipal user, @RequestParam("file") MultipartFile[] files, @RequestParam Optional<String> path) {
        log.info("put on " + path.orElse("/"));
        minioService.putFolder(minioService.generateStorageName(user.getId()), files, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @GetMapping(value = "/delete")
    String deleteFile(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName, @RequestParam Optional<String> path) {
        minioService.deleteObject(minioService.generateStorageName(user.getId()), fileName);
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @GetMapping(value = "/delete/{fileName}/")
    String deleteFolder(@AuthenticationPrincipal MyPrincipal user, @PathVariable String fileName, @RequestParam Optional<String> path) {
        minioService.deleteObject(minioService.generateStorageName(user.getId()), fileName + "/");
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @GetMapping(value = "/deleteFolder")
    String deleteAllFileInFolder(@AuthenticationPrincipal MyPrincipal user, @RequestParam("folderName") String folderName,
                                 @RequestParam Optional<String> path) {
        minioService.deleteFolder(minioService.generateStorageName(user.getId()), folderName, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/createFolder")
    String createFolder(@AuthenticationPrincipal MyPrincipal user, @RequestParam("folderName") String folderName, @RequestParam Optional<String> path) {
        minioService.createFolder(minioService.generateStorageName(user.getId()), path.orElse("") + folderName);
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/transferFile")
    String transferFile(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName,
                        @RequestParam Optional<String> path, @RequestParam Optional<String> folderName) {
        minioService.transferObject(minioService.generateStorageName(user.getId()), fileName, folderName.orElse("") + "/");
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/renameFile")
    String renameFile(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName, @RequestParam String fileNameNew,
                      @RequestParam Optional<String> path, @RequestParam Optional<String> folderName) {
        minioService.renameObject(minioService.generateStorageName(user.getId()),
                fileName, path.orElse("") + fileNameNew);
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/renameFolder")
    String renameFolder(@AuthenticationPrincipal MyPrincipal user, @RequestParam String folderName, @RequestParam String folderNameNew,
                        @RequestParam Optional<String> path) {
        minioService.renameFolder(minioService.generateStorageName(user.getId()), folderName, folderNameNew, path.orElse(""));
        return "redirect:/storage?path=" + pathNameUtils.encode(path.orElse(""));
    }

    @PostMapping("/search")
    String postSearch(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName,
                      @RequestParam Optional<String> path, Model model) {
        return "redirect:/storage/search?fileName=" + pathNameUtils.encode(fileName);

    }

    @GetMapping("/search")
    String getSearch(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName,
                     @RequestParam Optional<String> path, Model model) {
        var list = minioService.searchFile(minioService.generateStorageName(user.getId()), fileName);
        model.addAttribute("objectList", list);
        model.addAttribute("path", path.orElse(""));
        model.addAttribute("backPath", pathNameUtils.getBackPath(path.orElse("/")));
        return "storage";
    }


}
