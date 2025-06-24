package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.AuthRequest;
import com.go4champ.go4champ.model.AuthResponse;
import com.go4champ.go4champ.security.JwtTokenUtil;
import com.go4champ.go4champ.service.UserService;
import com.go4champ.go4champ.service.EmailService;
import com.go4champ.go4champ.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "AuthController", description = "API für Authentifizierung")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Registriert einen neuen User")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body("Username bereits vergeben");
            }

            // Prüfen ob E-Mail bereits existiert
            if (user.getEmail() != null && userService.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("E-Mail-Adresse bereits registriert");
            }

            // Verification Token generieren
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setEmailVerified(false);

            // User speichern
            User savedUser = userService.createUser(user);

            // Verification E-Mail senden (falls E-Mail angegeben)
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendVerificationEmail(user.getEmail(), verificationToken);
                return ResponseEntity.ok("Registrierung erfolgreich! Bitte bestätigen Sie Ihre E-Mail-Adresse über den Link in der E-Mail.");
            } else {
                return ResponseEntity.ok("Registrierung erfolgreich!");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der Registrierung: " + e.getMessage());
        }
    }

    @Operation(summary = "E-Mail-Adresse bestätigen")
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            User user = userService.findByVerificationToken(token);
            if (user == null) {
                return ResponseEntity.badRequest().body("Ungültiger oder abgelaufener Verification-Token");
            }

            // E-Mail als bestätigt markieren
            user.setEmailVerified(true);
            user.setVerificationToken(null); // Token löschen
            userService.updateUser(user);

            return ResponseEntity.ok("✅ E-Mail-Adresse erfolgreich bestätigt! Sie können sich jetzt vollständig anmelden.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der E-Mail-Bestätigung: " + e.getMessage());
        }
    }

    @Operation(summary = "Login für bestehende User")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Prüfen ob E-Mail bestätigt wurde
            User user = userService.getUserById(authRequest.getUsername());
            if (user != null && user.getEmail() != null && !user.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("⚠️ Bitte bestätigen Sie zuerst Ihre E-Mail-Adresse über den Link in der E-Mail.");
            }

            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(token, userDetails.getUsername()));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Ungültige Anmeldedaten");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login Fehler: " + e.getMessage());
        }
    }

    @Operation(summary = "Validation E-Mail erneut senden")
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("E-Mail-Adresse nicht gefunden");
            }

            if (user.isEmailVerified()) {
                return ResponseEntity.badRequest().body("E-Mail-Adresse ist bereits bestätigt");
            }

            // Neuen Token generieren
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            userService.updateUser(user);

            // E-Mail erneut senden
            emailService.sendVerificationEmail(email, verificationToken);

            return ResponseEntity.ok("Bestätigungs-E-Mail wurde erneut gesendet");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Senden der E-Mail: " + e.getMessage());
        }
    }

    @Operation(summary = "Validiert JWT Token")
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    return ResponseEntity.ok("Token ist gültig für User: " + username);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ungültiger Token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Validierung fehlgeschlagen");
        }
    }
}