package com.go4champ.go4champ.dto;

import jakarta.validation.constraints.*;

/**
 * DTO für die Sieger-Bestimmung bei FREE Type Challenges
 */
public class DeclareWinnerRequest {

    @NotBlank(message = "Winner Username ist erforderlich")
    private String winnerUsername; // "challenger", "challenged" oder "tie"

    @Size(max = 200, message = "Reason darf maximal 200 Zeichen haben")
    private String reason; // Begründung

    // Constructors
    public DeclareWinnerRequest() {}

    public DeclareWinnerRequest(String winnerUsername, String reason) {
        this.winnerUsername = winnerUsername;
        this.reason = reason;
    }

    // Getters and Setters
    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}