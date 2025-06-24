package com.go4champ.go4champ.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für ein komplettes Ranking
 */
public class RankingResponse {

    private String rankingType; // "TOTAL_TRAININGS", "MONTHLY_TRAININGS", "STREAK", etc.
    private String title; // "Gesamte Trainings", "Trainings diesen Monat", etc.
    private String description;
    private String period; // "Allzeit", "Dezember 2024", "Diese Woche", etc.

    private List<RankingEntry> entries;
    private int totalEntries;
    private int currentUserPosition;
    private boolean currentUserInTopList;

    // Metadata
    private LocalDateTime lastUpdated;
    private String updateFrequency; // "Echtzeit", "Täglich", "Wöchentlich"

    // Constructors
    public RankingResponse() {}

    public RankingResponse(String rankingType, String title, List<RankingEntry> entries) {
        this.rankingType = rankingType;
        this.title = title;
        this.entries = entries;
        this.totalEntries = entries.size();
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRankingType() { return rankingType; }
    public void setRankingType(String rankingType) { this.rankingType = rankingType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<RankingEntry> getEntries() { return entries; }
    public void setEntries(List<RankingEntry> entries) { this.entries = entries; }

    public int getTotalEntries() { return totalEntries; }
    public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }

    public int getCurrentUserPosition() { return currentUserPosition; }
    public void setCurrentUserPosition(int currentUserPosition) { this.currentUserPosition = currentUserPosition; }

    public boolean isCurrentUserInTopList() { return currentUserInTopList; }
    public void setCurrentUserInTopList(boolean currentUserInTopList) { this.currentUserInTopList = currentUserInTopList; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getUpdateFrequency() { return updateFrequency; }
    public void setUpdateFrequency(String updateFrequency) { this.updateFrequency = updateFrequency; }
}