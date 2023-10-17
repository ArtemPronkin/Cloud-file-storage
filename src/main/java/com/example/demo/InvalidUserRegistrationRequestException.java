package com.example.demo;

public class InvalidUserRegistrationRequestException extends RuntimeException {
    public InvalidUserRegistrationRequestException(String message) {
        super(message);
    }
}
