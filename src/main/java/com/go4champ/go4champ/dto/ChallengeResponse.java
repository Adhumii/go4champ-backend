package com.go4champ.go4champ.dto;

import com.go4champ.go4champ.model.ChallengeType;
import com.go4champ.go4champ.model.ChallengeStatus;
import java.time.LocalDateTime;

/**
 * DTO für Challenge Antworten
 */
public class ChallengeResponse {

    private Long id;
    private String challengerUsername;
    private String challengerName;
    private String challengedUsername;
    private String challengedName;
    private ChallengeType type;
    private ChallengeStatus status;
    private String title;
    private String description;
    private Double targetValue;
    private String targetUnit;
    private Double challengerResult;
    private Double challengedResult;
    private String winnerUsername;
    private String winnerName;
    private boolean challengerSubmitted;
    private boolean challengedSubmitted;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deadline;
    private boolean isExpired;
    private String myRole; // "CHALLENGER" oder "CHALLENGED"

    // Constructors
    public ChallengeResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChallengerUsername() { return challengerUsername; }
    public void setChallengerUsername(String challengerUsername) { this.challengerUsername = challengerUsername; }

    public String getChallengerName() { return challengerName; }
    public void setChallengerName(String challengerName) { this.challengerName = challengerName; }

    public String getChallengedUsername() { return challengedUsername; }
    public void setChallengedUsername(String challengedUsername) { this.challengedUsername = challengedUsername; }

    public String getChallengedName() { return challengedName; }
    public void setChallengedName(String challengedName) { this.challengedName = challengedName; }

    public ChallengeType getType() { return type; }
    public void setType(ChallengeType type) { this.type = type; }

    public ChallengeStatus getStatus() { return status; }
    public void setStatus(ChallengeStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTargetValue() { return targetValue; }
    public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }

    public String getTargetUnit() { return targetUnit; }
    public void setTargetUnit(String targetUnit) { this.targetUnit = targetUnit; }

    public Double getChallengerResult() { return challengerResult; }
    public void setChallengerResult(Double challengerResult) { this.challengerResult = challengerResult; }

    public Double getChallengedResult() { return challengedResult; }
    public void setChallengedResult(Double challengedResult) { this.challengedResult = challengedResult; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }

    public boolean isChallengerSubmitted() { return challengerSubmitted; }
    public void setChallengerSubmitted(boolean challengerSubmitted) { this.challengerSubmitted = challengerSubmitted; }

    public boolean isChallengedSubmitted() { return challengedSubmitted; }
    public void setChallengedSubmitted(boolean challengedSubmitted) { this.challengedSubmitted = challengedSubmitted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public String getMyRole() { return myRole; }
    public void setMyRole(String myRole) { this.myRole = myRole; }

    // Convenience Methods
    public boolean canSubmitResult(String username) {
        if (!status.equals(ChallengeStatus.ACCEPTED)) {
            return false;
        }

        if (challengerUsername.equals(username)) {
            return !challengerSubmitted;
        } else if (challengedUsername.equals(username)) {
            return !challengedSubmitted;
        }
        return false;
    }

    public boolean canDeclareWinner(String username) {
        return type.equals(ChallengeType.FREE) &&
                challengerUsername.equals(username) &&
                challengerSubmitted &&
                challengedSubmitted &&
                winnerUsername == null;
    }
}