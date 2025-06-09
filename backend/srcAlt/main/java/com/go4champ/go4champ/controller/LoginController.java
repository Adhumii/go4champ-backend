package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

    @Controller
    public class LoginController {

        @Autowired
        private UserRepo userRepo;

        @GetMapping("/login")
        public String showLoginPage() {
            return "Login";
        }

        @PostMapping("/login")
        public String login(@RequestParam String username, @RequestParam String password, Model model) {
            Optional<User> userOptional = userRepo.findById(username);
            if (userOptional.isPresent() && password.equals(userOptional.get().getPassword())) {
                return "redirect:/home";

            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
}

    }

