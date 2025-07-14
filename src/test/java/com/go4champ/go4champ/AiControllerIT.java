package com.go4champ.go4champ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go4champ.go4champ.dto.AiRequest;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepo userRepo;

    @Test
    @WithMockUser(username = "mockuser")
    public void testGenerateAndSavePlan_withMockUser() throws Exception {
        // Benutzer-Dummy
        User mockUser = new User();
        mockUser.setUsername("mockuser");
        mockUser.setEmail("mockuser@test.com");
        mockUser.setAge(25);
        mockUser.setWeight(75);
        mockUser.setWeightGoal(70);
        mockUser.setHeight(180);
        mockUser.setGender("MALE");
        mockUser.setEmailVerified(true);

        // Repository simulieren
        when(userRepo.findByUsername("mockuser")).thenReturn(Optional.of(mockUser));

        // Request Body
        AiRequest aiRequest = new AiRequest();

        mockMvc.perform(post("/api/ai/chat-create-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aiRequest)))
                .andExpect(status().is5xxServerError()); // 500 ist korrekt, da keine echte KI-Antwort zurückkommt
    }
}
