package com.go4champ.go4champ.dto;

import java.util.List;

/**
 * DTO für Friend Rankings (nur Freunde anzeigen)
 */
public class FriendRankingResponse {

    private String rankingType;
    private String title;
    private String period;
    private List<RankingEntry> friendEntries;
    private RankingEntry currentUser; // Position des aktuellen Users
    private int totalFriends;

    // Constructors
    public FriendRankingResponse() {}

    public FriendRankingResponse(String rankingType, String title, List<RankingEntry> friendEntries, RankingEntry currentUser) {
        this.rankingType = rankingType;
        this.title = title;
        this.friendEntries = friendEntries;
        this.currentUser = currentUser;
        this.totalFriends = friendEntries.size();
    }

    // Getters and Setters
    public String getRankingType() { return rankingType; }
    public void setRankingType(String rankingType) { this.rankingType = rankingType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<RankingEntry> getFriendEntries() { return friendEntries; }
    public void setFriendEntries(List<RankingEntry> friendEntries) { this.friendEntries = friendEntries; }

    public RankingEntry getCurrentUser() { return currentUser; }
    public void setCurrentUser(RankingEntry currentUser) { this.currentUser = currentUser; }

    public int getTotalFriends() { return totalFriends; }
    public void setTotalFriends(int totalFriends) { this.totalFriends = totalFriends; }
}