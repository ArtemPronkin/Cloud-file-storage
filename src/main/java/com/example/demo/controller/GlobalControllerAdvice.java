package com.example.demo.controller;

import com.example.demo.exception.S3StorageFileNotFoundException;
import com.example.demo.exception.S3StorageResourseIsOccupiedException;
import com.example.demo.exception.S3StorageServerException;
import com.example.demo.util.PathNameUtils;
import jakarta.servlet.ServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    PathNameUtils pathNameUtils;

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(S3StorageServerException.class)
    public String S3StorageServerException(S3StorageServerException e, Model model) {
        return "error500";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(S3StorageFileNotFoundException.class)
    public String S3StorageFileNotFoundException(Model model, ServletResponse response) {
        return "error404";
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(S3StorageResourseIsOccupiedException.class)
    public String S3StorageResourseIsOccupiedException(Model model, ServletResponse response) {
        return "error409";
    }
}

