package com.example.demo.controller;


import com.example.demo.exception.S3StorageServerException;
import com.example.demo.exception.UserUniqueEmailException;
import com.example.demo.exception.UserUniqueUserNameException;
import com.example.demo.model.User;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.S3StorageService;
import com.example.demo.service.S3StorageServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class Registration {

    S3StorageServiceInterface s3StorageService;
    RegistrationService registration;

    public Registration(@Qualifier("workStorageService")  S3StorageServiceInterface s3StorageService, RegistrationService registration) {
        this.s3StorageService = s3StorageService;
        this.registration = registration;
    }

    @GetMapping("/registration")
    String showRegPage(@ModelAttribute("user") User user) {

        return "registration";
    }

    @PostMapping("/registration")
    String createNewProfile(@ModelAttribute("user") @Valid User user,
                            BindingResult bindingResult) throws S3StorageServerException {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            user = registration.registration(user);
        } catch (UserUniqueUserNameException e) {
            bindingResult.rejectValue("username", "", "This username is already taken");
        } catch (UserUniqueEmailException e) {
            bindingResult.rejectValue("email", "", "This email is already taken");
        }
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        s3StorageService.makeWorkDirectory(s3StorageService.generateStorageName(user.getId()));
        return "redirect:/login";
    }
}
