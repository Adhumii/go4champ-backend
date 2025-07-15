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

    @Value("${anthropic.api.key}")
    private String apiKey;

    private String callCloudAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);  // Key sicher geladen
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-opus-20240229");
        requestBody.put("max_tokens", 3000);
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

            String prompt = String.format("""
Du bist ein **streng beschränkter KI-Fitness-Coach**.  
Du **darfst ausschließlich** über folgende Themen sprechen:

- Trainingspläne
- Übungen
- Fitnessziele
- Körperliche Gesundheit
- Geräte im Training
- Motivation zum Sport

Du **darfst keine Fragen** beantworten zu anderen Themen – wie z. B. Politik, Geschichte, Informatik, Psychologie, Ernährung, Religion oder persönlichen Meinungen.  
Wenn eine Frage nicht zum Fitnessbereich gehört, **musst du immer exakt folgendes sagen**:

"Dazu kann ich nichts sagen."

---

### Deine Aufgabe:

Erstelle für folgende Person bis zu 4 **Trainingsobjekte** mit dieser Struktur:

{
  "title": "Name des Trainings (String)",
  "duration": Dauer in Minuten (int, max. 20),
  "difficulty": Schwierigkeitsgrad von 1.0 bis 5.0 (float),
  "typeString": "Indoor" oder "Outdoor" (String),
  "description": "Trainingsbeschreibung (String)"
}

Daten der Person:
- Alter: %d Jahre
- Gewicht: %d kg
- Zielgewicht: %d kg
- Größe: %d cm
- Geschlecht: %s
- Verfügbare Geräte: %s

### Wichtige Regeln:
- Gib ausschließlich eine gültige JSON-Liste mit 1–4 Trainingsobjekten zurück
- Keine Kommentare, kein Markdown, keine Erklärungen
- Wenn der Nutzer keine trainingsbezogene Anfrage stellt, gib **keine JSON-Ausgabe** und antworte nur mit:  
  `"Dazu kann ich nichts sagen."`

""", user.getAge(), user.getWeight(), user.getWeightGoal(), user.getHeight(), user.getGender(), String.join(", ", user.getAvailableEquipment()));


            String aiReply = callCloudAI(prompt);

            ObjectMapper mapper = new ObjectMapper();
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
