package com.go4champ.go4champ.dto;

import com.go4champ.go4champ.model.ChallengeType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO für das Erstellen einer neuen Challenge
 */
public class CreateChallengeRequest {

    @NotBlank(message = "Challenged username ist erforderlich")
    private String challengedUsername;

    @NotNull(message = "Challenge Type ist erforderlich")
    private ChallengeType type;

    @NotBlank(message = "Titel ist erforderlich")
    @Size(max = 200, message = "Titel darf maximal 200 Zeichen haben")
    private String title;

    @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen haben")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Target Value muss größer als 0 sein")
    private Double targetValue; // Für REPS und WEIGHT

    @Size(max = 50, message = "Target Unit darf maximal 50 Zeichen haben")
    private String targetUnit; // z.B. "Wiederholungen", "kg"

    private LocalDateTime deadline; // Optional, sonst 7 Tage Standard

    // Constructors
    public CreateChallengeRequest() {}

    public CreateChallengeRequest(String challengedUsername, ChallengeType type, String title,
                                  String description, Double targetValue, String targetUnit, LocalDateTime deadline) {
        this.challengedUsername = challengedUsername;
        this.type = type;
        this.title = title;
        this.description = description;
        this.targetValue = targetValue;
        this.targetUnit = targetUnit;
        this.deadline = deadline;
    }

    // Getters and Setters
    public String getChallengedUsername() { return challengedUsername; }
    public void setChallengedUsername(String challengedUsername) { this.challengedUsername = challengedUsername; }

    public ChallengeType getType() { return type; }
    public void setType(ChallengeType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTargetValue() { return targetValue; }
    public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }

    public String getTargetUnit() { return targetUnit; }
    public void setTargetUnit(String targetUnit) { this.targetUnit = targetUnit; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}