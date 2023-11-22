package com.example.demo.Controller;

import com.example.demo.Exception.S3StorageException;
import com.example.demo.util.PathNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    PathNameUtils pathNameUtils;

    @ExceptionHandler(S3StorageException.class)
    public String s3StorageException(S3StorageException e, Model model) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("path", "");
        model.addAttribute("backPath", "");
        return "storage";
    }
}
