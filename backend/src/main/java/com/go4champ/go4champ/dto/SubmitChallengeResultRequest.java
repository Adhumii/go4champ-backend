package com.go4champ.go4champ.dto;

import jakarta.validation.constraints.*;

/**
 * DTO für das Einreichen eines Challenge Ergebnisses
 */
public class SubmitChallengeResultRequest {

    @NotNull(message = "Ergebnis ist erforderlich")
    @DecimalMin(value = "0.0", message = "Ergebnis muss mindestens 0 sein")
    private Double result;

    @Size(max = 200, message = "Kommentar darf maximal 200 Zeichen haben")
    private String comment; // Optional

    // Constructors
    public SubmitChallengeResultRequest() {}

    public SubmitChallengeResultRequest(Double result, String comment) {
        this.result = result;
        this.comment = comment;
    }

    // Getters and Setters
    public Double getResult() { return result; }
    public void setResult(Double result) { this.result = result; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}