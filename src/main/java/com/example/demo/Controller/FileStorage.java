package com.example.demo.Controller;

import com.example.demo.Service.MinioService;
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

@Slf4j
@Controller
@RequestMapping(value = "/storage")
public class FileStorage {
    @Autowired
    MinioService minioService;

    @GetMapping
    String getStorage(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        var list = minioService.listObjects(userDetails.getUsername());
        model.addAttribute("objectList", list);
        return "storage";
    }

    @GetMapping(value = "/{fileName}")
    void getStorage(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable String fileName, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setStatus(HttpServletResponse.SC_OK);
        var file = minioService.getObject(userDetails.getUsername(), fileName);
        try {
            FileCopyUtils.copy(file, response.getOutputStream());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    @PostMapping()
    String postStorage(@AuthenticationPrincipal UserDetails userDetails,
                       String command, String name, @RequestParam("file") MultipartFile file) throws IOException {
        minioService.putObject(userDetails.getUsername(), file.getOriginalFilename(), file.getContentType(), file.getInputStream());
        return "redirect:/storage";
    }

}
