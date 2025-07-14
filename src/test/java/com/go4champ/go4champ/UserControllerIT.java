package com.go4champ.go4champ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go4champ.go4champ.model.AuthRequest;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Test
    public void testRegisterAndLoginUser() throws Exception {
        String uniqueUsername = "testuser_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(uniqueUsername);
        user.setPassword("test123");
        user.setEmail(uniqueUsername + "@example.com");
        user.setName("Test User");
        user.setAge(25);
        user.setGender("MALE");
        user.setWeight(80);
        user.setWeightGoal(75);

        // Registrierung
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // E-Mail-Verifizierung manuell setzen
        User savedUser = userRepo.findById(uniqueUsername).orElseThrow();
        savedUser.setEmailVerified(true);
        userRepo.save(savedUser);

        // Login mit AuthRequest
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(uniqueUsername);
        authRequest.setPassword("test123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());
    }


    @Test
    public void testRegisterUserWithExistingUsername() throws Exception {
        String username = "duplicate_user_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setEmail(username + "@example.com");
        user.setName("Test");
        user.setAge(22);
        user.setGender("FEMALE");
        user.setWeight(70);
        user.setWeightGoal(65);

        // Erste Registrierung (soll erfolgreich sein)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Zweite Registrierung mit gleichem Username (soll fehlschlagen)
        user.setEmail("andere@example.com"); // andere Email, gleicher Username
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginWithWrongPassword() throws Exception {
        String username = "user_with_wrong_pw_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(username);
        user.setPassword("correct123");
        user.setEmail(username + "@example.com");
        user.setName("Test User");
        user.setAge(20);
        user.setGender("MALE");
        user.setWeight(85);
        user.setWeightGoal(80);

        // Registrierung
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // E-Mail-Verifizierung manuell setzen
        User savedUser = userRepo.findById(username).orElseThrow();
        savedUser.setEmailVerified(true);
        userRepo.save(savedUser);

        // Login mit falschem Passwort
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginWithoutVerifiedEmail() throws Exception {
        String username = "unverified_" + System.currentTimeMillis();

        User user = new User();
        user.setUsername(username);
        user.setPassword("mypassword");
        user.setEmail(username + "@example.com");
        user.setName("Unverified User");
        user.setAge(30);
        user.setGender("MALE");
        user.setWeight(90);
        user.setWeightGoal(85);

        // Registrierung (E-Mail bleibt unbestätigt)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Login-Versuch ohne E-Mail-Bestätigung
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword("mypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden());
    }


}
