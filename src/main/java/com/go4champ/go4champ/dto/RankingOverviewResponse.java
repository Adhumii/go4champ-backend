package com.go4champ.go4champ.dto;

import java.util.List;

/**
 * DTO für Ranking Übersicht (alle Kategorien auf einen Blick)
 */
public class RankingOverviewResponse {

    private UserStats currentUserStats;

    // Top 3 in verschiedenen Kategorien
    private List<RankingEntry> topTrainingsAllTime;
    private List<RankingEntry> topTrainingsThisMonth;
    private List<RankingEntry> topStreaks;
    private List<RankingEntry> topChallengeWinners;
    private List<RankingEntry> topConsistency;

    // User Position in verschiedenen Rankings
    private int myOverallPosition;
    private int myMonthlyPosition;
    private int myStreakPosition;
    private int myChallengePosition;
    private int myConsistencyPosition;

    // Freunde-spezifische Rankings
    private FriendRankingResponse friendsThisMonth;
    private FriendRankingResponse friendsStreak;

    // Achievements/Milestones
    private List<String> recentAchievements;
    private List<String> upcomingMilestones;

    // Constructors
    public RankingOverviewResponse() {}

    // Getters and Setters
    public UserStats getCurrentUserStats() { return currentUserStats; }
    public void setCurrentUserStats(UserStats currentUserStats) { this.currentUserStats = currentUserStats; }

    public List<RankingEntry> getTopTrainingsAllTime() { return topTrainingsAllTime; }
    public void setTopTrainingsAllTime(List<RankingEntry> topTrainingsAllTime) { this.topTrainingsAllTime = topTrainingsAllTime; }

    public List<RankingEntry> getTopTrainingsThisMonth() { return topTrainingsThisMonth; }
    public void setTopTrainingsThisMonth(List<RankingEntry> topTrainingsThisMonth) { this.topTrainingsThisMonth = topTrainingsThisMonth; }

    public List<RankingEntry> getTopStreaks() { return topStreaks; }
    public void setTopStreaks(List<RankingEntry> topStreaks) { this.topStreaks = topStreaks; }

    public List<RankingEntry> getTopChallengeWinners() { return topChallengeWinners; }
    public void setTopChallengeWinners(List<RankingEntry> topChallengeWinners) { this.topChallengeWinners = topChallengeWinners; }

    public List<RankingEntry> getTopConsistency() { return topConsistency; }
    public void setTopConsistency(List<RankingEntry> topConsistency) { this.topConsistency = topConsistency; }

    public int getMyOverallPosition() { return myOverallPosition; }
    public void setMyOverallPosition(int myOverallPosition) { this.myOverallPosition = myOverallPosition; }

    public int getMyMonthlyPosition() { return myMonthlyPosition; }
    public void setMyMonthlyPosition(int myMonthlyPosition) { this.myMonthlyPosition = myMonthlyPosition; }

    public int getMyStreakPosition() { return myStreakPosition; }
    public void setMyStreakPosition(int myStreakPosition) { this.myStreakPosition = myStreakPosition; }

    public int getMyChallengePosition() { return myChallengePosition; }
    public void setMyChallengePosition(int myChallengePosition) { this.myChallengePosition = myChallengePosition; }

    public int getMyConsistencyPosition() { return myConsistencyPosition; }
    public void setMyConsistencyPosition(int myConsistencyPosition) { this.myConsistencyPosition = myConsistencyPosition; }

    public FriendRankingResponse getFriendsThisMonth() { return friendsThisMonth; }
    public void setFriendsThisMonth(FriendRankingResponse friendsThisMonth) { this.friendsThisMonth = friendsThisMonth; }

    public FriendRankingResponse getFriendsStreak() { return friendsStreak; }
    public void setFriendsStreak(FriendRankingResponse friendsStreak) { this.friendsStreak = friendsStreak; }

    public List<String> getRecentAchievements() { return recentAchievements; }
    public void setRecentAchievements(List<String> recentAchievements) { this.recentAchievements = recentAchievements; }

    public List<String> getUpcomingMilestones() { return upcomingMilestones; }
    public void setUpcomingMilestones(List<String> upcomingMilestones) { this.upcomingMilestones = upcomingMilestones; }
}