package com.example.demo.exception;

public class InvalidUserRegistrationRequestException extends RuntimeException {
    public InvalidUserRegistrationRequestException(String message) {
        super(message);
    }
}
