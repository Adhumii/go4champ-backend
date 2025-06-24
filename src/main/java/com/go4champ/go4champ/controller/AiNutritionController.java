package com.go4champ.go4champ.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go4champ.go4champ.dto.AiRequest;
import com.go4champ.go4champ.model.Meal;
import com.go4champ.go4champ.model.NutritionPlan;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import com.go4champ.go4champ.service.MealService;
import com.go4champ.go4champ.service.NutritionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ObjectMapper mapper = new ObjectMapper();

    private String callCloudAI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", "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"); // Aus Sicherheitsgründen in .env auslagern
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-3-opus-20240229");
        body.put("max_tokens", 1200);
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
                Erstelle mir einen personalisierten Ernährungsplan mit genau 4 Mahlzeiten (Frühstück, Mittagessen, Snack, Abendessen).
                Die Person hat folgende Werte:
                Alter: %d Jahre
                Gewicht: %d kg
                Zielgewicht: %d kg

                Jede Mahlzeit soll folgende Eigenschaften enthalten:
                - title (String)
                - type (Frühstück, Mittagessen, Snack, Abendessen)
                - description (String)
                - calories (int, kcal)

                Antwort nur als JSON-Array!
                """, user.getAge(), user.getWeight(), user.getWeightGoal());

            String aiResponse = callCloudAI(prompt);

            List<Meal> meals;
            try {
                meals = mapper.readValue(aiResponse, new TypeReference<List<Meal>>() {});
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
                // Wenn du plan.addMeal() nutzt, stelle sicher, dass es existiert
            }

            return ResponseEntity.ok(Map.of("antwort", aiResponse, "planName", plan.getPlanName()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Fehler: " + e.getMessage()));
        }
    }
}