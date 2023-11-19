package com.example.demo.Controller;


import com.example.demo.Exceptions.UserUniqueEmailExceptions;
import com.example.demo.Exceptions.UserUniqueUserNameExceptions;
import com.example.demo.Service.RegistrationService;
import com.example.demo.Service.S3StorageService;
import com.example.demo.model.User;
import com.example.demo.util.UserValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class Registration {
    @Autowired
    S3StorageService s3StorageService;
    @Autowired
    RegistrationService registration;
    @Autowired
    private UserValidator userValidator;

    @GetMapping("/registration")
    String showRegPage(@ModelAttribute("user") User user) {

        return "registration";
    }

    @PostMapping("/registration")
    String createNewProfile(@ModelAttribute("user") @Valid User user,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "/registration";
        }
        try {
            user = registration.registration(user);
        } catch (UserUniqueUserNameExceptions e) {
            bindingResult.rejectValue("username", "", "This username is already taken");
        } catch (UserUniqueEmailExceptions e) {
            bindingResult.rejectValue("email", "", "This email is already taken");
        }
        if (bindingResult.hasErrors()) {
            return "/registration";
        }
        s3StorageService.makeBucket(s3StorageService.generateStorageName(user.getId()));
        return "redirect:/login";
    }
}
