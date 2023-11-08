package com.example.demo.util;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            errors.rejectValue("email","","This email is already taken");
        }
        if (userRepository.findByUsername(user.getEmail()).isPresent()) {
            errors.rejectValue("username","","This username is already taken");
        }
    }
}
