package com.go4champ.go4champ.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailTestService {

    private static final Logger logger = LoggerFactory.getLogger(EmailTestService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private String port;

    public void testEmailConfig() {
        logger.info("=== E-MAIL KONFIGURATION TEST ===");
        logger.info("Host: {}", host);
        logger.info("Port: {}", port);
        logger.info("Username: {}", fromEmail);
        logger.info("Password Length: {}", password != null ? password.length() : "null");
        logger.info("Password (first 4 chars): {}", password != null && password.length() > 4 ? password.substring(0, 4) + "..." : password);

        try {
            logger.info("Teste SMTP-Verbindung...");

            // Einfache Test-Mail
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("Go4Champ@proton.me");
            message.setSubject("Go4Champ - E-Mail Test");
            message.setText("Das ist eine Test-E-Mail von Go4Champ!");
            message.setFrom(fromEmail);

            mailSender.send(message);
            logger.info(" E-MAIL ERFOLGREICH VERSENDET!");

        } catch (Exception e) {
            logger.error(" E-MAIL FEHLER: {}", e.getMessage());
            logger.error(" FEHLER TYP: {}", e.getClass().getSimpleName());

            if (e.getMessage().contains("Authentication")) {
                logger.error(" AUTHENTIFIZIERUNG FEHLGESCHLAGEN!");
                logger.error("   → Prüfe App-Passwort");
                logger.error("   → Prüfe Gmail-Account");
            }

            if (e.getMessage().contains("Connection")) {
                logger.error(" VERBINDUNGSPROBLEM!");
                logger.error("   → Prüfe Internet");
                logger.error("   → Prüfe Firewall");
            }

            e.printStackTrace();
        }
        logger.info("=== TEST ENDE ===");
    }
}