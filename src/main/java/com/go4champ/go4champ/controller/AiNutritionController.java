package com.go4champ.go4champ.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go4champ.go4champ.dto.AiRequest;
import com.go4champ.go4champ.model.Meal;
import com.go4champ.go4champ.model.NutritionPlan;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import com.go4champ.go4champ.service.MealService;
import com.go4champ.go4champ.service.NutritionPlanService;
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
@RequestMapping("/api/ai/nutrition")
public class AiNutritionController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NutritionPlanService nutritionPlanService;

    @Autowired
    private MealService mealService;

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    private String callCloudAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);  // Key sicher geladen
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-3-opus-20240229");
        body.put("max_tokens", 3000);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages", requestEntity, Map.class
        );

        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.getBody().get("content");
        return (String) contentList.get(0).get("text");
    }

    @PostMapping("/create-plan")
    public ResponseEntity<?> generateNutritionPlan(@RequestBody AiRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepo.findByUsername(username).orElse(null);

            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Benutzer nicht gefunden"));
            }

            String prompt = String.format("""
    
    Du bist ein professioneller Ernährungsberater und sollst einen individuellen Ernährungsplan mit bis zu 4 Gerichten erstellen. 
    Die Person hat folgende Merkmale:
    - Alter: %d Jahre
    - Gewicht: %d kg
    - Zielgewicht: %d kg
    - Größe: %d cm
    - Geschlecht: %s

    Für jede Mahlzeit erstellst du ein **einzelnes JSON-Objekt** mit folgenden Feldern:

    {
      "name": "Titel des Gerichts",
      "type": "Frühstück | Mittagessen | Snack | Abendessen",
      "description": "Kurze Beschreibung der Mahlzeit",
      "calories": Ganzzahl,
      "protein": Ganzzahl in g,
      "fat": Ganzzahl in g,
      "carbs": Ganzzahl in g,
      "ingredients": ["Zutat 1", "Zutat 2", "..."],
      "instructions": ["Schritt 1", "Schritt 2", "..."]
    }

    Gib ein **JSON-Array** mit bis zu 4 dieser Objekte zurück. 
    Keine Einleitung, keine Erklärungen, keine Formatierungen – nur sauberes JSON.
    """, user.getAge(), user.getWeight(), user.getWeightGoal(), user.getHeight(), user.getGender());

            String aiResponse = callCloudAI(prompt);

            List<Meal> meals;
            try {
                // 1. Zuerst den AI-Response-String (enthält JSON-Array als String!) einmal in echte JSON-Struktur parsen
                JsonNode node = mapper.readTree(aiResponse);

                // 2. Prüfen ob es ein Array ist
                if (!node.isArray()) {
                    return ResponseEntity.status(400).body(Map.of(
                            "error", "KI-Antwort ist kein JSON-Array",
                            "antwort", aiResponse
                    ));
                }

                // 3. Konvertiere das JSON-Array in eine Liste von Meal-Objekten
                meals = mapper.readValue(node.toString(), new TypeReference<List<Meal>>() {});
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Ungültige KI-Antwort. Kein JSON erkannt.",
                        "antwort", aiResponse
                ));
            }


            NutritionPlan plan = new NutritionPlan();
            plan.setPlanName("KI-Ernährungsplan vom " + LocalDate.now());
            plan.setUser(user);
            nutritionPlanService.savePlan(plan);

            for (Meal meal : meals) {
                meal.setUser(user);
                meal.setNutritionPlan(plan);
                mealService.save(meal);
                // Optional: plan.addMeal(meal); falls du diese Methode in NutritionPlan ergänzt
            }

            return ResponseEntity.ok(Map.of(
                    "antwort", aiResponse,
                    "planName", plan.getPlanName()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Fehler: " + e.getMessage()));
        }
    }
}



//@RestController
//@RequestMapping("/api/ai/nutrition")
//public class AiNutritionController {
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @Autowired
//    private NutritionPlanService nutritionPlanService;
//
//    @Autowired
//    private MealService mealService;
//
//    @Value("${anthropic.api.key}")
//    private String apiKey;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    private String callCloudAI(String prompt) {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("x-api-key", apiKey);
//        headers.set("anthropic-version", "2023-06-01");
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("model", "claude-3-opus-20240229");
//        body.put("max_tokens", 1200);
//        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
//
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                "https://api.anthropic.com/v1/messages", requestEntity, Map.class
//        );
//
//        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.getBody().get("content");
//        return (String) contentList.get(0).get("text");
//    }
//
//    @PostMapping("/create-plan")
//    public ResponseEntity<?> generateNutritionPlan(@RequestBody AiRequest request) {
//        try {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            String username = auth.getName();
//            User user = userRepo.findByUsername(username).orElse(null);
//
//            if (user == null) {
//                return ResponseEntity.status(404).body(Map.of("error", "Benutzer nicht gefunden"));
//            }
//
//            // Prompt angepasst mit Nutzereingaben aus request
//            String prompt = String.format("""
//                 Du bist ein Ernährungsberater und sollst einen auf die Person zugeschnittenen Ernährungsplan mit bis zu 4 Mahlzeiten erstellen:
//                       – Frühstück
//                       – Mittagessen
//                       – Snack
//                       – Abendessen
//
//                    Und den Benutzerdaten:
//                    - Alter: %d Jahre
//                    - Gewicht: %d kg
//                    - Zielgewicht: %d kg
//                    - Größe: %d cm
//                    - Geschlecht: %s
//
//                    Erstelle entweder:
//                    - Eine vollständige Tages-Ernährungsplan (mit Frühstück, Mittagessen, Snack, Abendessen)
//                    ODER
//                    - Ein einzelnes Rezept (je nach Eingabe)
//
//                    Struktur eines Rezepts:
//                    {
//                      "title": "String – Name der Mahlzeit",
//                      "type": "Frühstück | Mittagessen | Snack | Abendessen",
//                      "description": "kurze Beschreibung",
//                      "calories": int
//                    }
//
//                    Regeln:
//                    - Gib **nur ein JSON-Array** mit 1–4 Rezeptobjekten zurück.
//                    - Keine Kommentare oder Erklärungen.
//                    - Kein Markdown.
//                    - Nur sauberes JSON.
//                    """,
//                    user.getAge(),
//                    user.getWeight(),
//                    user.getWeightGoal(),
//                    user.getHeight(),
//                    user.getGender()
//            );
//
//            String aiResponse = callCloudAI(prompt);
//
//            List<Meal> meals;
//            try {
//                JsonNode node = mapper.readTree(aiResponse);
//                if (!node.isArray()) {
//                    return ResponseEntity.status(400).body(Map.of("error", "KI-Antwort ist kein JSON-Array", "antwort", aiResponse));
//                }
//                meals = mapper.readValue(node.toString(), new TypeReference<List<Meal>>() {});
//            } catch (JsonProcessingException e) {
//                return ResponseEntity.status(400).body(Map.of("error", "Ungültige KI-Antwort. Kein JSON erkannt.", "antwort", aiResponse));
//            }
//
//            NutritionPlan plan = new NutritionPlan();
//            plan.setPlanName("KI-Ernährungsplan vom " + LocalDate.now());
//            plan.setUser(user);
//            nutritionPlanService.savePlan(plan);
//
//            for (Meal meal : meals) {
//                meal.setUser(user);
//                meal.setNutritionPlan(plan);
//                mealService.save(meal);
//            }
//
//            return ResponseEntity.ok(Map.of(
//                    "antwort", aiResponse,
//                    "planName", plan.getPlanName()
//            ));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("error", "Fehler: " + e.getMessage()));
//        }
//    }
//}
