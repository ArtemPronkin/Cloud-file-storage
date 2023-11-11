package com.example.demo.Controller;

import com.example.demo.Service.MinioService;
import com.example.demo.Service.MyPrincipal;
import io.minio.Result;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping
    String storage(@AuthenticationPrincipal MyPrincipal user, Model model, @RequestParam Optional<String> path) {
        var list = minioService.listPathObjects(minioService.generateStorageName(user.getId()),path.orElse(""));
        model.addAttribute("objectList", list);
        model.addAttribute("path", path.orElse(""));
        log.info(path.orElse("nothing"));
        return "storage";
    }

    @GetMapping(value = "/download")
    void getFile(@AuthenticationPrincipal MyPrincipal user, @RequestParam String fileName, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setStatus(HttpServletResponse.SC_OK);
        var file = minioService.getObject(minioService.generateStorageName(user.getId()), fileName);
        try {
            FileCopyUtils.copy(file, response.getOutputStream());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @PostMapping("/putFile")
    String putFile(@AuthenticationPrincipal MyPrincipal user,@RequestParam("file") MultipartFile file,@RequestParam Optional<String> path) throws IOException {
        log.info("put on " + path.orElse("/"));
        minioService.putObject(minioService.generateStorageName(user.getId()),path.orElse("") + file.getOriginalFilename(), file.getContentType(), file.getInputStream());

        return "redirect:/storage";
    }
    @GetMapping(value = "/delete")
    String deleteFile(@AuthenticationPrincipal MyPrincipal user,@RequestParam String fileName){
        minioService.deleteObject(minioService.generateStorageName(user.getId()), fileName);
        return "redirect:/storage";
    }
    @GetMapping(value = "/delete/{fileName}/")
    String deleteFolder(@AuthenticationPrincipal MyPrincipal user,@PathVariable String fileName){
        minioService.deleteObject(minioService.generateStorageName(user.getId()), fileName+"/");
        return "redirect:/storage";
    }

    @PostMapping("createFolder")
    String createFolder(@AuthenticationPrincipal MyPrincipal user,@RequestParam("folderName") String folderName , @RequestParam Optional<String> path) throws IOException {
        minioService.createFolder(minioService.generateStorageName(user.getId()),path.orElse("")+folderName);
        return "redirect:/storage";
    }


}
