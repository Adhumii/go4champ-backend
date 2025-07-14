//package com.go4champ.go4champ;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.go4champ.go4champ.dto.AiRequest;
//import com.go4champ.go4champ.model.User;
//import com.go4champ.go4champ.repo.UserRepo;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class NutritionPlanIT {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Test
//    public void testGenerateNutritionPlan() throws Exception {
//        // ✅ Benutzer mit zufälligem Namen erzeugen
//        String randomUsername = "testuser_" + UUID.randomUUID();
//
//        User user = new User();
//        user.setUsername(randomUsername);
//        user.setPassword(passwordEncoder.encode("test123"));
//        user.setEmail(randomUsername + "@example.com");
//        user.setEmailVerified(true); // wichtig!
//        user.setAge(30);
//        user.setWeight(75);
//        user.setWeightGoal(70);
//        user.setHeight(180);
//        user.setGender("MALE");
//        userRepo.save(user);
//
//        // 🔐 Login-Request
//        String loginJson = """
//            {
//              "username": "%s",
//              "password": "test123"
//            }
//        """.formatted(randomUsername);
//
//        String token = mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(loginJson))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        // Extrahiere das Token aus der Antwort
//        String jwt = mapper.readTree(token).get("token").asText();
//
//        // 📦 Ernährungplan anfordern
//        AiRequest aiRequest = new AiRequest();
//        String jsonRequest = mapper.writeValueAsString(aiRequest);
//
//        mockMvc.perform(post("/api/ai/nutrition/create-plan")
//                        .header("Authorization", "Bearer " + jwt)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonRequest))
//                .andExpect(status().isOk());
//    }
//}
