package com.example.demo;

import com.example.demo.Exception.UserUniqueEmailException;
import com.example.demo.Exception.UserUniqueUserNameException;
import com.example.demo.Service.MyUserDetailsService;
import com.example.demo.Service.RegistrationService;
import com.example.demo.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.TransactionSystemException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class RegistrationServiceTests {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql");
    @Autowired
    RegistrationService registration;

    @Autowired
    MyUserDetailsService myUserDetailsService;

    @Test
    void registrationTest() throws UserUniqueUserNameException, UserUniqueEmailException {
        String name = "vasya";
        registration.registration(new User(name, "vasya@vasya", "password"));
        Assertions.assertEquals(name, myUserDetailsService.loadUserByUsername("vasya").getUsername());
    }

    @Test
    void uniqueUsernameTest() throws UserUniqueUserNameException, UserUniqueEmailException {
        String name = "vasya2";
        String email = "vasya@vasya2";
        String password = "pass";
        var user = new User(name, email, password);
        registration.registration(user);
        Assertions.assertThrows(UserUniqueEmailException.class,
                () -> registration.registration(new User(name + "test", email, password)));
        Assertions.assertThrows(UserUniqueUserNameException.class,
                () -> registration.registration(new User(name, email + "test", password)));

    }

    @Test
    void uniqueValidTest() throws UserUniqueUserNameException, UserUniqueEmailException {
        String name = "vasya3";
        String email = "notEmail";
        String password = "pass";
        var user = new User(name, email, password);
        Assertions.assertThrows(TransactionSystemException.class, () -> registration.registration(user));
    }
}
