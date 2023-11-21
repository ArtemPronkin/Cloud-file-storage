package com.example.demo.Controller;

import com.example.demo.Exception.S3StorageException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(S3StorageException.class)
    public String s3StorageException(S3StorageException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "storage";
    }
}
