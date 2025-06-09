package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.TrainingsPlan;
import com.go4champ.go4champ.security.JwtTokenUtil;
import com.go4champ.go4champ.service.TrainingsPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Tag(name = "TrainingsPlanController", description = "API für Trainingspläne")
@RestController
@RequestMapping("/api/training-plans")
public class TrainingsPlanController {

    @Autowired
    private TrainingsPlanService trainingsPlanService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Operation(summary = "Alle Trainingspläne abrufen")
    @GetMapping
    public ResponseEntity<?> getAllTrainingPlans() {
        try {
            List<TrainingsPlan> plans = trainingsPlanService.getAllTrainingPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der Trainingspläne: " + e.getMessage());
        }
    }

    @Operation(summary = "Trainingsplan anhand der ID abrufen")
    @GetMapping("/{planId}")
    public ResponseEntity<?> getTrainingsPlanById(@PathVariable int planId) {
        try {
            Optional<TrainingsPlan> plan = trainingsPlanService.getTrainingsPlanById(planId);

            if (plan.isPresent()) {
                return ResponseEntity.ok(plan.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Trainingsplan mit ID " + planId + " nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen: " + e.getMessage());
        }
    }

    @Operation(summary = "Alle Trainingspläne eines Users abrufen")
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getTrainingsPlansByUsername(@PathVariable String username) {
        try {
            List<TrainingsPlan> plans = trainingsPlanService.getTrainingsPlansByUsername(username);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen: " + e.getMessage());
        }
    }

    @Operation(summary = "Trainingspläne des eingeloggten Users abrufen")
    @GetMapping("/me")
    public ResponseEntity<?> getMyTrainingPlans(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            List<TrainingsPlan> plans = trainingsPlanService.getTrainingsPlansByUsername(username);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der Trainingspläne: " + e.getMessage());
        }
    }

    @Operation(summary = "Neuen Trainingsplan erstellen")
    @PostMapping
    public ResponseEntity<?> createTrainingsPlan(
            @RequestBody TrainingsPlan trainingsPlan,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            TrainingsPlan createdPlan = trainingsPlanService.createTrainingsPlan(trainingsPlan, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fehler beim Erstellen: " + e.getMessage());
        }
    }

    @Operation(summary = "Trainingsplan aktualisieren")
    @PutMapping("/{planId}")
    public ResponseEntity<?> updateTrainingsPlan(
            @PathVariable int planId,
            @RequestBody TrainingsPlan trainingsPlan) {
        try {
            TrainingsPlan updatedPlan = trainingsPlanService.updateTrainingsPlan(planId, trainingsPlan);
            return ResponseEntity.ok(updatedPlan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fehler beim Aktualisieren: " + e.getMessage());
        }
    }

    @Operation(summary = "Trainingsplan löschen")
    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deleteTrainingsPlan(@PathVariable int planId) {
        try {
            trainingsPlanService.deleteTrainingsPlan(planId);
            return ResponseEntity.ok("Trainingsplan erfolgreich gelöscht");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Löschen: " + e.getMessage());
        }
    }

    @Operation(summary = "Training zu einem Plan hinzufügen")
    @PostMapping("/{planId}/trainings/{trainingId}")
    public ResponseEntity<?> addTrainingToPlan(
            @PathVariable int planId,
            @PathVariable int trainingId) {
        try {
            TrainingsPlan updatedPlan = trainingsPlanService.addTrainingToPlan(planId, trainingId);
            return ResponseEntity.ok(updatedPlan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fehler beim Hinzufügen: " + e.getMessage());
        }
    }

    @Operation(summary = "Training aus einem Plan entfernen")
    @DeleteMapping("/{planId}/trainings/{trainingId}")
    public ResponseEntity<?> removeTrainingFromPlan(
            @PathVariable int planId,
            @PathVariable int trainingId) {
        try {
            TrainingsPlan updatedPlan = trainingsPlanService.removeTrainingFromPlan(planId, trainingId);
            return ResponseEntity.ok(updatedPlan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fehler beim Entfernen: " + e.getMessage());
        }
    }

    @Operation(summary = "Suche nach Trainingsplänen anhand des Namens")
    @GetMapping("/search")
    public ResponseEntity<?> searchTrainingsPlansByName(@RequestParam String name) {
        try {
            List<TrainingsPlan> plans = trainingsPlanService.searchTrainingsPlansByName(name);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der Suche: " + e.getMessage());
        }
    }

    @Operation(summary = "Trainingspläne mit mindestens X Trainings")
    @GetMapping("/min-trainings/{minCount}")
    public ResponseEntity<?> getTrainingsPlansByMinTrainingCount(@PathVariable int minCount) {
        try {
            List<TrainingsPlan> plans = trainingsPlanService.getTrainingsPlansByMinTrainingCount(minCount);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen: " + e.getMessage());
        }
    }

    @Operation(summary = "Prüfen ob Planname bereits existiert")
    @GetMapping("/exists/{planName}")
    public ResponseEntity<?> checkPlanNameExists(@PathVariable String planName) {
        try {
            boolean exists = trainingsPlanService.planNameExists(planName);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der Prüfung: " + e.getMessage());
        }
    }
}