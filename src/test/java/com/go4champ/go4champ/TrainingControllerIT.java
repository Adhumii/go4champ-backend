package com.go4champ.go4champ;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TrainingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingForMockUser() throws Exception {
        String trainingJson = """
                {
                    "title": "Ganzkörpertraining",
                    "duration": 45,
                    "difficulty": 2.5,
                    "typeString": "Outdoor",
                    "description": "Kombi aus Laufen, Push-Ups und Planks"
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainingJson))
                .andExpect(status().isCreated());
    }


    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithInvalidData() throws Exception {
        String invalidTrainingJson = """
                {
                    "title": "",
                    "duration": -30,
                    "difficulty": 5.0,
                    "typeString": "Indoor",
                    "description": ""
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTrainingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithMissingFields() throws Exception {
        String incompleteTrainingJson = """
                {
                    "title": "Cardio",
                    "duration": 30
                    // Fehlende Felder wie difficulty, typeString und description
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteTrainingJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithInvalidTypeString() throws Exception {
        String invalidTypeStringJson = """
                {
                    "title": "Yoga",
                    "duration": 60,
                    "difficulty": 1.0,
                    "typeString": "InvalidType", // Ungültiger Typ
                    "description": "Entspannendes Yoga-Training"
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTypeStringJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithValidData() throws Exception {
        String validTrainingJson = """
                {
                    "title": "Krafttraining",
                    "duration": 60,
                    "difficulty": 3.5,
                    "typeString": "Indoor",
                    "description": "Intensives Krafttraining mit Gewichten"
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTrainingJson))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Leerer JSON-Body
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithNullFields() throws Exception {
        String nullFieldsJson = """
                {
                    "title": null,
                    "duration": 30,
                    "difficulty": 2.0,
                    "typeString": "Indoor",
                    "description": null
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullFieldsJson))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithExcessiveDuration() throws Exception {
        String excessiveDurationJson = """
                {
                    "title": "Marathon Training",
                    "duration": 300, // Übermäßige Dauer
                    "difficulty": 4.0,
                    "typeString": "Outdoor",
                    "description": "Intensives Marathon-Training"
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(excessiveDurationJson))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testCreateTrainingWithInvalidDifficulty() throws Exception {
        String invalidDifficultyJson = """
                {
                    "title": "Fortgeschrittenes Training",
                    "duration": 45,
                    "difficulty": 6.0, // Ungültiger Schwierigkeitsgrad
                    "typeString": "Indoor",
                    "description": "Herausforderndes Training für Fortgeschrittene"
                }
                """;

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDifficultyJson))
                .andExpect(status().isBadRequest());
    }
}
