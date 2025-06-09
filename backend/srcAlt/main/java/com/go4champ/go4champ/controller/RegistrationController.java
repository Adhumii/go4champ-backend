package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }


    @PostMapping("/register")
    public String register( @ModelAttribute("user")User user, Model model) {
        if (userRepo.existsById(user.getUsername())) {
            model.addAttribute("error", "User already exists");
            return "register";
        }
        userRepo.save(user);
        return "redirect:/";

    }

}