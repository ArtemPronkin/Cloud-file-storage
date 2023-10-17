package com.example.demo.Controller;


import com.example.demo.InvalidUserRegistrationRequestException;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Controller
public class NewProfile {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registration")
    String showRegPage() {

        return "registration";
    }

    @PostMapping("/registration")
    RedirectView createNewProfile(@ModelAttribute("user") User user,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  ModelMap modelMap) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new InvalidUserRegistrationRequestException("User already exists");
        }
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return new RedirectView("/login");
    }
}
