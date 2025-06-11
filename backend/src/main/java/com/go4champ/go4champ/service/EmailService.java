package com.go4champ.go4champ.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // URLs aus application.properties lesen
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Go4Champ - Passwort zurücksetzen");
            message.setText(createResetEmailText(resetToken));
            message.setFrom("go4champ.sender2025@gmail.com");

            mailSender.send(message);
            logger.info("✅ Password Reset E-Mail erfolgreich gesendet!");
            logger.info("Von: go4champ.sender2025@gmail.com");
            logger.info("An: {}", toEmail);
            logger.info("Token: {}", resetToken);

        } catch (Exception e) {
            logger.error("❌ E-Mail Fehler: {}", e.getMessage());

            logger.warn("FALLBACK - Token in Konsole:");
            logger.info("=== PASSWORD RESET EMAIL ===");
            logger.info("Empfänger: {}", toEmail);
            logger.info("Token: {}", resetToken);
            logger.info("Link: {}/reset-password?token={}", frontendUrl, resetToken);
            logger.info("============================");
        }
    }

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Go4Champ - E-Mail-Adresse bestätigen");
            message.setText(createVerificationEmailText(verificationToken));
            message.setFrom("go4champ.sender2025@gmail.com");

            mailSender.send(message);
            logger.info("✅ Verification E-Mail erfolgreich gesendet!");
            logger.info("Von: go4champ.sender2025@gmail.com");
            logger.info("An: {}", toEmail);
            logger.info("Verification Token: {}", verificationToken);

        } catch (Exception e) {
            logger.error("❌ Verification E-Mail Fehler: {}", e.getMessage());

            logger.warn("FALLBACK - Verification Token in Konsole:");
            logger.info("=== EMAIL VERIFICATION ===");
            logger.info("Empfänger: {}", toEmail);
            logger.info("Token: {}", verificationToken);
            logger.info("Link: {}/api/auth/verify-email?token={}", backendUrl, verificationToken);
            logger.info("===========================");
        }
    }

    private String createResetEmailText(String resetToken) {
        return "Hallo,\n\n" +
                "Sie haben eine Passwort-Zurücksetzung für Ihr Go4Champ-Konto angefordert.\n\n" +
                "Klicken Sie auf den folgenden Link, um Ihr Passwort zurückzusetzen:\n" +
                frontendUrl + "/reset-password?token=" + resetToken + "\n\n" +
                "Dieser Link ist 24 Stunden gültig.\n\n" +
                "Falls Sie diese Anfrage nicht gestellt haben, ignorieren Sie diese E-Mail.\n\n" +
                "Mit freundlichen Grüßen,\n" +
                "Ihr Go4Champ Team";
    }

    private String createVerificationEmailText(String verificationToken) {
        return "Willkommen bei Go4Champ!\n\n" +
                "Vielen Dank für Ihre Registrierung. Um Ihr Konto zu aktivieren, bestätigen Sie bitte Ihre E-Mail-Adresse.\n\n" +
                "Klicken Sie auf den folgenden Link zur Bestätigung:\n" +
                backendUrl + "/api/auth/verify-email?token=" + verificationToken + "\n\n" +
                "Dieser Link ist 24 Stunden gültig.\n\n" +
                "Nach der Bestätigung können Sie sich vollständig in Ihr Go4Champ-Konto einloggen.\n\n" +
                "Falls Sie sich nicht registriert haben, ignorieren Sie diese E-Mail.\n\n" +
                "Mit freundlichen Grüßen,\n" +
                "Ihr Go4Champ Team";
    }
}
