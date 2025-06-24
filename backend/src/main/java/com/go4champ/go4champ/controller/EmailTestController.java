

package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.service.EmailTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    @Autowired
    private EmailTestService emailTestService;

    @GetMapping("/email")
    public String testEmail() {
        emailTestService.testEmailConfig();
        return "E-Mail Test ausgeführt - siehe Konsole für Details!";
    }
}