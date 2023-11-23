package com.example.demo.exception;

public class UserUniqueEmailException extends Exception {
    public UserUniqueEmailException(String message) {
        super(message);
    }
}
