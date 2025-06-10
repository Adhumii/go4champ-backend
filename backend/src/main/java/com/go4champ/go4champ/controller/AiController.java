package com.go4champ.go4champ.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go4champ.go4champ.dto.AiRequest;
import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.model.TrainingsPlan;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import com.go4champ.go4champ.service.TrainingService;
import com.go4champ.go4champ.service.TrainingsPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainingsPlanService trainingsPlanService;

    private String callCloudAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", "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"); // sichere den Schlüssel in .env
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-opus-20240229");
        requestBody.put("max_tokens", 1000);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = "https://api.anthropic.com/v1/messages";

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.getBody().get("content");
        return (String) contentList.get(0).get("text");
    }

    @PostMapping("/chat-create-plan")
    public ResponseEntity<?> generateAndSavePlan(@RequestBody AiRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Benutzer nicht gefunden"));
            }

            // Prompt für JSON mit Trainingsobjekten
            String prompt = String.format("""
Gib mir eine JSON-Liste mit genau 3 Trainingsobjekten. Die Trainingsobjekte sind für eine Person mit:
Alter: %d, Gewicht: %d kg, Zielgewicht: %d kg.

Jedes Objekt soll enthalten:
- title (String)
- duration (int)
- difficulty (float, 1.0–5.0)
- typeString (String: Indoor oder Outdoor)
- description (String)

Antwort **nur mit JSON**, ohne Text davor oder danach.
""", user.getAge(), user.getWeight(), user.getWeightGoal());

            // KI-Antwort abrufen
            String aiReply = callCloudAI(prompt);

            // JSON in Java-Objekte parsen
            ObjectMapper mapper = new ObjectMapper();
            List<Training> trainings = mapper.readValue(aiReply, new TypeReference<>() {});

            // Trainingsplan erstellen und speichern
            TrainingsPlan plan = new TrainingsPlan();
            plan.setPlanName("KI-Plan vom " + LocalDate.now());
            plan.setDescription("Automatisch generierter Plan");
            plan.setUser(user);
            trainingsPlanService.createTrainingsPlan(plan, username);

            // Trainingsobjekte zuweisen und speichern
            for (Training training : trainings) {
                training.setUser(user);
                training.setTrainingsPlan(plan);
                training.setTypeString(training.getTypeString()); // konvertiert Indoor/Outdoor zu true/false
                trainingService.createTraining(training);
                plan.addTraining(training);
            }

            return ResponseEntity.ok(Map.of(
                    "antwort", aiReply,
                    "plan", plan.getPlanName()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Fehler: " + e.getMessage()));
        }
    }
}
