package com.example.demo.service.security;

import com.example.demo.exception.UserUniqueEmailException;
import com.example.demo.exception.UserUniqueUserNameException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegistrationService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public User registration(User user) throws UserUniqueUserNameException, UserUniqueEmailException {
        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            var message = e.getMessage().split("'");
            if (user.getUsername().equals(message[1])) {
                throw new UserUniqueUserNameException("Username not unique");
            } else if (user.getEmail().equals(message[1])) {
                throw new UserUniqueEmailException("Email not unique");
            } else throw e;
        }
    }
}
