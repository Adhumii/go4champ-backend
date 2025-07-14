//package com.go4champ.go4champ;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.go4champ.go4champ.model.User;
//import com.go4champ.go4champ.repo.UserRepo;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//import java.util.List;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class TrainingPlanIT {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private UserRepo userRepo;
//
//    private String token;
//
//    @BeforeEach
//    public void setup() throws Exception {
//        // Dummy-User mit Verifizierung anlegen
//        User user = new User();
//        user.setUsername("traininguser");
//        user.setPassword("test123");
//        user.setEmail("training@example.com");
//        user.setEmailVerified(true);
//        user.setName("Test User");
//        user.setAge(28);
//        user.setGender("MALE");
//        user.setWeight(75);
//        user.setHeight(180);
//        user.setWeightGoal(70);
//        user.setAvailableEquipment(List.of("Hanteln", "Matte", "Springseil"));
//
//        if (userRepo.findByUsername(user.getUsername()).isEmpty()) {
//            userRepo.save(user);
//        }
//
//        // Login durchführen, um Token zu erhalten
//        var loginPayload = objectMapper.writeValueAsString(
//                new LoginRequest("traininguser", "test123")
//        );
//
//        var loginResponse = mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(loginPayload))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String responseBody = loginResponse.getResponse().getContentAsString();
//        this.token = objectMapper.readTree(responseBody).get("token").asText();
//    }
//
//    @Test
//    public void testGenerateTrainingPlan() throws Exception {
//        var request = new AiRequest();
//        request.setMessage("Erstelle bitte einen individuellen Trainingsplan");
//
//        mockMvc.perform(post("/api/ai/chat-create-plan")
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//    }
//
//    // Mini-Hilfsklasse für Login-Daten
//    static class LoginRequest {
//        public String username;
//        public String password;
//
//        public LoginRequest(String username, String password) {
//            this.username = username;
//            this.password = password;
//        }
//    }
//
//    // Dummy-Klasse für AiRequest (sofern du keine eigene DTO-Klasse hast)
//    static class AiRequest {
//        private String message;
//        public String getMessage() { return message; }
//        public void setMessage(String message) { this.message = message; }
//    }
//}
package com.go4champ.go4champ;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TrainingsPlanControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetAllTrainingPlans() throws Exception {
        mockMvc.perform(get("/api/training-plans"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTrainingsPlanById_notFound() throws Exception {
        mockMvc.perform(get("/api/training-plans/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTrainingsPlansByUsername() throws Exception {
        mockMvc.perform(get("/api/training-plans/user/testuser"))
                .andExpect(status().isOk());
    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testGetTrainingsPlansMe_invalidToken() throws Exception {
//        mockMvc.perform(get("/api/training-plans/me"))
//                .andExpect(status().isUnauthorized());
//    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testSearchTrainingsPlansByName_empty() throws Exception {
        mockMvc.perform(get("/api/training-plans/search")
                        .param("name", "NichtExistierenderPlan"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTrainingsPlansByMinTrainingCount() throws Exception {
        mockMvc.perform(get("/api/training-plans/min-trainings/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCheckPlanNameExists_false() throws Exception {
        mockMvc.perform(get("/api/training-plans/exists/NichtVorhanden"))
                .andExpect(status().isOk());
    }





    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testAddAndRemoveTrainingToPlan_NotFound() throws Exception {
        int dummyPlanId = 9999;
        int dummyTrainingId = 8888;

        // Hinzufügen schlägt fehl (Dummy-Daten)
        mockMvc.perform(post("/api/training-plans/" + dummyPlanId + "/trainings/" + dummyTrainingId))
                .andExpect(status().isNotFound());

        // Entfernen schlägt fehl (Dummy-Daten)
        mockMvc.perform(delete("/api/training-plans/" + dummyPlanId + "/trainings/" + dummyTrainingId))
                .andExpect(status().isNotFound());
    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testAddAndRemoveTrainingToPlan_Success() throws Exception {
//        int validPlanId = 1; // Angenommene gültige Plan-ID
//        int validTrainingId = 1; // Angenommene gültige Trainings-ID
//
//        // Hinzufügen eines Trainings zum Plan
//        mockMvc.perform(post("/api/training-plans/" + validPlanId + "/trainings/" + validTrainingId))
//                .andExpect(status().isOk());
//
//        // Entfernen des Trainings aus dem Plan
//        mockMvc.perform(delete("/api/training-plans/" + validPlanId + "/trainings/" + validTrainingId))
//                .andExpect(status().isOk());
//    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testCreateTrainingPlan() throws Exception {
//        String newPlanJson = """
//                {
//                    "name": "Neuer Trainingsplan",
//                    "description": "Ein Test-Trainingsplan",
//                    "trainings": []
//                }
//                """;
//
//        mockMvc.perform(post("/api/training-plans")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(newPlanJson))
//                .andExpect(status().isCreated());
//    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testDeleteTrainingsPlan_NotFound() throws Exception {
        int dummyPlanId = 9999;

        mockMvc.perform(delete("/api/training-plans/" + dummyPlanId))
                .andExpect(status().isNotFound());
    }
//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testDeleteTrainingsPlan_Success() throws Exception {
//        int validPlanId = 1; // Angenommene gültige Plan-ID
//
//        mockMvc.perform(delete("/api/training-plans/" + validPlanId))
//                .andExpect(status().isOk());
//    }
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCheckPlanNameExists_true() throws Exception {
        String existingPlanName = "VorhandenerPlan"; // Angenommener existierender Planname

        mockMvc.perform(get("/api/training-plans/exists/" + existingPlanName))
                .andExpect(status().isOk());
    }

}
