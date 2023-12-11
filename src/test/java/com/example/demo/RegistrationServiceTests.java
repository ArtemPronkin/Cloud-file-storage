package com.example.demo;

import com.example.demo.exception.UserUniqueEmailException;
import com.example.demo.exception.UserUniqueUserNameException;
import com.example.demo.model.User;
import com.example.demo.service.security.RegistrationService;
import com.example.demo.service.security.MyUserDetailsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
    RegistrationService registrationService;

    @Autowired
    MyUserDetailsService myUserDetailsService;

    @Test
    void Must_MySQLContainerIsRun_WhenTestsStarted() {
        Assertions.assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void Must_UserFound_WhenUserIsRegistered() throws UserUniqueUserNameException, UserUniqueEmailException {
        String name = "vasya";

        registrationService.registration(new User(name, "vasya@vasya", "password"));

        Assertions.assertEquals(name, myUserDetailsService.loadUserByUsername("vasya").getUsername());
    }

    @Test
    void Must_ThrowException_WhenTryToRegisterNonUniqueNameOrEmail() throws UserUniqueUserNameException, UserUniqueEmailException {
        String name = "vasya2";
        String email = "vasya@vasya2";
        String password = "pass";

        var user = new User(name, email, password);
        registrationService.registration(user);

        Assertions.assertThrows(UserUniqueEmailException.class,
                () -> registrationService.registration(new User(name + "test", email, password)));

        Assertions.assertThrows(UserUniqueUserNameException.class,
                () -> registrationService.registration(new User(name, email + "test", password)));

    }

    @Test
    void Must_ThrowException_WhenTryToRegisterWithNotCorrectEmail() {
        String name = "vasya3";
        String email = "notEmail";
        String password = "pass";

        var user = new User(name, email, password);

        Assertions.assertThrows(jakarta.validation.ConstraintViolationException.class, () -> registrationService.registration(user));
    }
}
