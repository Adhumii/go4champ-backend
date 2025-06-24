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
@RequestMapping("/api")
public class UserController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private UserService service;

    // =============================================================================
    // ADMIN ENDPUNKTE (für alle User)
    // =============================================================================

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

    @Operation(summary = "Holt einen User (Admin)")
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

    @Operation(summary = "Aktualisiert einen User (Admin)")
    @PutMapping("/user/{username}")
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

    // =============================================================================
    // /ME ENDPUNKTE (für eingeloggten User)
    // =============================================================================

    @Operation(summary = "Holt das vollständige Profil des eingeloggten Users")
    @GetMapping("/me/profile")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);

            // Token-Validierung mit besserer Fehlerbehandlung
            String username;
            try {
                username = jwtUtil.getUsernameFromToken(token);
            } catch (io.jsonwebtoken.SignatureException e) {
                System.err.println("JWT Signature Fehler: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ungültig - bitte erneut anmelden");
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                System.err.println("JWT Token abgelaufen: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token abgelaufen - bitte erneut anmelden");
            } catch (Exception e) {
                System.err.println("JWT Token Fehler: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token-Fehler - bitte erneut anmelden");
            }

            System.out.println("Profile request for user: " + username); // Debug log

            // User holen
            User user = service.getUserById(username);
            if (user != null) {
                System.out.println("User found: " + user.toString()); // Debug log
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen des Profils: " + e.getMessage()); // Debug log
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen des Profils: " + e.getMessage());
        }
    }

    @Operation(summary = "Aktualisiert das vollständige Profil des eingeloggten Users")
    @PutMapping("/me/profile")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody User profileData) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            System.out.println("Profile update for user: " + username); // Debug log
            System.out.println("Profile data: " + profileData.toString()); // Debug log

            // Sicherstellen, dass Username nicht geändert wird
            profileData.setUsername(username);

            // User aktualisieren
            User updatedUser = service.updateUser(profileData);
            if (updatedUser != null) {
                System.out.println("Profile updated successfully"); // Debug log
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Aktualisieren des Profils: " + e.getMessage()); // Debug log
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Aktualisieren des Profils: " + e.getMessage());
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

    @Operation(summary = "Holt Basis-Infos des eingeloggten Users")
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Basis-Info zurückgeben
            User user = service.getUserById(username);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                        "username", user.getUsername(),
                        "name", user.getName() != null ? user.getName() : "",
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "roles", user.getRoles()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der User-Info: " + e.getMessage());
        }
    }
}