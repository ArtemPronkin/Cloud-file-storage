package com.example.demo.Controller;


import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
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
    @Autowired
    private UserValidator userValidator;

    @GetMapping("/registration")
    String showRegPage(@ModelAttribute("user") User user) {

        return "registration";
    }

    @PostMapping("/registration")
    String createNewProfile(@ModelAttribute("user") @Valid  User user,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        userValidator.validate(user,bindingResult);
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            model.addAttribute("errorLogin","User exists");
//            return "registration";
//        }
//        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
//            model.addAttribute("errorLogin","User exists");
//            return "registration";
//        }
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("errorLogin","Not valid");
//            return "registration";
//        }
        if (bindingResult.hasErrors()){
            return "/registration";
        }
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }
}
