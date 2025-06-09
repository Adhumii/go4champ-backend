// Temporäre Debug-Version - ersetze deine aktuelle PasswordResetService.java

package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.PasswordResetToken;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.PasswordResetTokenRepo;
import com.go4champ.go4champ.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private PasswordResetTokenRepo tokenRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void initiatePasswordReset(String email) {
        logger.info("=== DEBUG: Password reset initiated for email: {}", email);

        // Debug: Alle User ausgeben
        List<User> allUsers = userRepository.findAll();
        logger.info("=== DEBUG: Found {} users in database", allUsers.size());
        for (User user : allUsers) {
            logger.info("=== DEBUG: User - Username: {}, Email: {}", user.getUsername(), user.getEmail());
        }

        // Versuche User zu finden
        Optional<User> userOpt = null;

        // Methode 1: findByEmail (falls vorhanden)
        try {
            userOpt = userRepository.findByEmail(email);
            logger.info("=== DEBUG: findByEmail result: {}", userOpt.isPresent() ? "FOUND" : "NOT FOUND");
        } catch (Exception e) {
            logger.warn("=== DEBUG: findByEmail method not available: {}", e.getMessage());

            // Methode 2: Manuell suchen
            userOpt = allUsers.stream()
                    .filter(user -> email.equals(user.getEmail()))
                    .findFirst();
            logger.info("=== DEBUG: Manual search result: {}", userOpt.isPresent() ? "FOUND" : "NOT FOUND");
        }

        if (userOpt.isEmpty()) {
            logger.warn("=== DEBUG: No user found with email: {}", email);
            return;
        }

        User foundUser = userOpt.get();
        logger.info("=== DEBUG: Found user: {} with email: {}", foundUser.getUsername(), foundUser.getEmail());

        // Alte Tokens löschen
        tokenRepository.deleteByEmail(email);
        logger.info("=== DEBUG: Deleted old tokens for email: {}", email);

        // Neuen Token erstellen
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

        PasswordResetToken resetToken = new PasswordResetToken(token, email, expiryDate);
        tokenRepository.save(resetToken);
        logger.info("=== DEBUG: Created new token: {}", token);

        // E-Mail versenden
        emailService.sendPasswordResetEmail(email, token);

        logger.info("=== DEBUG: Password reset process completed successfully for: {}", email);
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isExpired() && !resetToken.isUsed();
    }

    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Ungültiger Token");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token ist abgelaufen");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token wurde bereits verwendet");
        }

        // User finden - mit Debug
        List<User> allUsers = userRepository.findAll();
        Optional<User> userOpt = allUsers.stream()
                .filter(user -> resetToken.getEmail().equals(user.getEmail()))
                .findFirst();

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Benutzer nicht gefunden");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token als verwendet markieren
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        logger.info("Password successfully reset for email: {}", resetToken.getEmail());
    }
}