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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainingsPlanService trainingsPlanService;

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    private String callCloudAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-opus-20240229");
        requestBody.put("max_tokens", 3000);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://api.anthropic.com/v1/messages", entity, Map.class);

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

            String prompt = String.format("""
Du bist ein KI-gestützter Fitness-Coach und darfst **ausschließlich Trainingspläne und Trainingsübungen** erstellen.

Wenn der Nutzer eine Frage stellt, die **nicht zum Bereich Sport und Fitness gehört**, musst du exakt sagen:
"Dazu kann ich nichts sagen."

---

### Aufgabe:
Erstelle **1–4 Trainingsobjekte** im JSON-Format auf Grundlage der folgenden Personendaten:

- Alter: %d Jahre
- Gewicht: %d kg
- Zielgewicht: %d kg
- Größe: %d cm
- Geschlecht: %s
- Verfügbare Geräte: %s

### Jedes Trainingsobjekt hat folgendes Format:

{
  "title": "String",
  "duration": 10–20 (int),
  "difficulty": 1.0–5.0 (float),
  "typeString": "Indoor" oder "Outdoor",
  "description": "String"
}

### Regeln:
- Gib **nur** eine JSON-Liste von max. 4 Objekten zurück.
- Verwende kein Markdown, keine Kommentare, keine Erklärungen.
- Wenn die Anfrage nicht zum Thema passt, gib **nur diesen Satz** aus:
"Dazu kann ich nichts sagen."
""", user.getAge(), user.getWeight(), user.getWeightGoal(), user.getHeight(), user.getGender(), String.join(", ", user.getAvailableEquipment()));

            String aiReply = callCloudAI(prompt).trim();

            // Wenn die KI mit dem Ablehnungssatz geantwortet hat → kein Plan erstellen
            if (aiReply.equalsIgnoreCase("Dazu kann ich nichts sagen.")) {
                return ResponseEntity.ok(Map.of("antwort", aiReply));
            }

            List<Training> trainings = mapper.readValue(aiReply, new TypeReference<>() {});
            TrainingsPlan plan = new TrainingsPlan();
            plan.setPlanName("KI-Plan vom " + LocalDate.now());
            plan.setDescription("Automatisch generierter Plan");
            plan.setUser(user);
            trainingsPlanService.createTrainingsPlan(plan, username);

            for (Training training : trainings) {
                training.setUser(user);
                training.setTrainingsPlan(plan);
                training.setTypeString(training.getTypeString());
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
