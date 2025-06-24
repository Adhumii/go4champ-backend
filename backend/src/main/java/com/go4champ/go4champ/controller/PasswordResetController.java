

package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.dto.PasswordResetRequest;
import com.go4champ.go4champ.dto.PasswordResetConfirm;
import com.go4champ.go4champ.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Falls ein Konto mit dieser E-Mail-Adresse existiert, wurde eine E-Mail mit Anweisungen zum Zurücksetzen des Passworts gesendet.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in forgot password: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (!isValid) {
                response.put("message", "Token ist ungültig oder abgelaufen");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Fehler bei der Token-Validierung");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetConfirm request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Passwort wurde erfolgreich zurückgesetzt");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error resetting password: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Unexpected error resetting password: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ein unerwarteter Fehler ist aufgetreten");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}