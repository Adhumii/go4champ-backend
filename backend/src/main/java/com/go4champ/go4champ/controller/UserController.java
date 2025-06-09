package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.security.JwtTokenUtil;
import com.go4champ.go4champ.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "UserController", description = "Api für User")
@RestController
public class UserController {
    @Autowired
    private JwtTokenUtil jwtUtil;
    @Autowired
    private UserService service;

    @Operation(summary = "Gibt alle User zurück")
    @GetMapping("/allUsers")
    public ResponseEntity<?> getAllUser() {
        try {
            List<User> users = service.getAllUser();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der User: " + e.getMessage());
        }
    }

    @Operation(summary = "Löscht einen bestimmten User")
    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            System.out.println("Versuche User zu löschen: " + username);

            if (!service.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User '" + username + "' nicht gefunden");
            }

            boolean deleted = service.delete(username);
            if (deleted) {
                return ResponseEntity.ok("User '" + username + "' erfolgreich gelöscht");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Fehler beim Löschen des Users");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Löschen: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Löschen: " + e.getMessage());
        }
    }

    @Operation(summary = "Holt einen User")
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        try {
            User user = service.getUserById(username);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User '" + username + "' nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen: " + e.getMessage());
        }
    }

    @Operation(summary = "Aktualisiert einen User")
    @PutMapping("/updateUser/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody User user) {
        try {
            if (!service.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User '" + username + "' nicht gefunden");
            }

            user.setUsername(username);
            User updatedUser = service.updateUser(user);

            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Fehler beim Aktualisieren");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Aktualisieren: " + e.getMessage());
        }
    }

    // NEU: Equipment Management Endpoints
    @Operation(summary = "Aktualisiert das verfügbare Equipment des eingeloggten Users")
    @PutMapping("/me/equipment")
    public ResponseEntity<?> updateMyEquipment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, List<String>> equipmentData) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // User holen und Equipment aktualisieren
            User user = service.getUserById(username);
            if (user != null) {
                List<String> equipment = equipmentData.get("availableEquipment");
                if (equipment != null) {
                    user.setAvailableEquipment(equipment);
                    User updatedUser = service.updateUser(user);
                    return ResponseEntity.ok(updatedUser);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Equipment-Liste fehlt");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Aktualisieren des Equipments: " + e.getMessage());
        }
    }

    @Operation(summary = "Holt das verfügbare Equipment des eingeloggten Users")
    @GetMapping("/me/equipment")
    public ResponseEntity<?> getMyEquipment(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // User und sein Equipment holen
            User user = service.getUserById(username);
            if (user != null) {
                return ResponseEntity.ok(Map.of("availableEquipment", user.getAvailableEquipment()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen des Equipments: " + e.getMessage());
        }
    }

    @Operation(summary = "Fügt einzelnes Equipment hinzu")
    @PostMapping("/me/equipment/{equipmentName}")
    public ResponseEntity<?> addEquipment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String equipmentName) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // User holen und Equipment hinzufügen
            User user = service.getUserById(username);
            if (user != null) {
                user.addEquipment(equipmentName.toUpperCase());
                User updatedUser = service.updateUser(user);
                return ResponseEntity.ok(Map.of(
                        "message", "Equipment hinzugefügt",
                        "availableEquipment", updatedUser.getAvailableEquipment()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Hinzufügen des Equipments: " + e.getMessage());
        }
    }

    @Operation(summary = "Entfernt einzelnes Equipment")
    @DeleteMapping("/me/equipment/{equipmentName}")
    public ResponseEntity<?> removeEquipment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String equipmentName) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // User holen und Equipment entfernen
            User user = service.getUserById(username);
            if (user != null) {
                user.removeEquipment(equipmentName.toUpperCase());
                User updatedUser = service.updateUser(user);
                return ResponseEntity.ok(Map.of(
                        "message", "Equipment entfernt",
                        "availableEquipment", updatedUser.getAvailableEquipment()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Entfernen des Equipments: " + e.getMessage());
        }
    }

    @Operation(summary = "Holt alle Trainingseinheiten des eingeloggten Users")
    @GetMapping("/me/trainings")
    public ResponseEntity<?> getMyTrainings(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // User und seine Trainings holen
            User user = service.getUserById(username);
            if (user != null) {
                return ResponseEntity.ok(user.getTrainings());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der Trainings: " + e.getMessage());
        }
    }

    @Operation(summary = "Holt alle Trainingspläne des eingeloggten Users")
    @GetMapping("/me/training-plans")
    public ResponseEntity<?> getMyTrainingPlans(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // User und seine Trainingspläne holen
            User user = service.getUserById(username);
            if (user != null) {
                return ResponseEntity.ok(user.getTrainingPlans());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der Trainingspläne: " + e.getMessage());
        }
    }
}