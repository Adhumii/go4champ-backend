package com.go4champ.go4champ.dto;

import java.time.LocalDateTime;

/**
 * DTO für User-Statistiken im Ranking System
 */
public class UserStats {

    private String username;
    private String name;
    private String avatarId;

    // Training Statistiken
    private long totalTrainings;
    private long monthlyTrainings;
    private long weeklyTrainings;
    private long todayTrainings;

    // Challenge Statistiken
    private long totalChallenges;
    private long wonChallenges;
    private long lostChallenges;
    private double challengeWinRate;

    // Streak & Consistency
    private int currentStreak;
    private int longestStreak;
    private double consistencyScore; // 0-100%

    // Training Qualität
    private double averageDifficulty;
    private double maxDifficulty;
    private long totalTrainingTime; // in Minuten
    private double averageTrainingTime;

    // Activity
    private LocalDateTime lastTrainingDate;
    private boolean hasTrainedToday;
    private int daysActive; // Tage mit mindestens einem Training

    // Ranking Positions
    private int overallRank;
    private int monthlyRank;
    private int streakRank;
    private int challengeRank;
    private int consistencyRank;

    // Constructors
    public UserStats() {}

    public UserStats(String username, String name) {
        this.username = username;
        this.name = name;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public long getTotalTrainings() { return totalTrainings; }
    public void setTotalTrainings(long totalTrainings) { this.totalTrainings = totalTrainings; }

    public long getMonthlyTrainings() { return monthlyTrainings; }
    public void setMonthlyTrainings(long monthlyTrainings) { this.monthlyTrainings = monthlyTrainings; }

    public long getWeeklyTrainings() { return weeklyTrainings; }
    public void setWeeklyTrainings(long weeklyTrainings) { this.weeklyTrainings = weeklyTrainings; }

    public long getTodayTrainings() { return todayTrainings; }
    public void setTodayTrainings(long todayTrainings) { this.todayTrainings = todayTrainings; }

    public long getTotalChallenges() { return totalChallenges; }
    public void setTotalChallenges(long totalChallenges) { this.totalChallenges = totalChallenges; }

    public long getWonChallenges() { return wonChallenges; }
    public void setWonChallenges(long wonChallenges) { this.wonChallenges = wonChallenges; }

    public long getLostChallenges() { return lostChallenges; }
    public void setLostChallenges(long lostChallenges) { this.lostChallenges = lostChallenges; }

    public double getChallengeWinRate() { return challengeWinRate; }
    public void setChallengeWinRate(double challengeWinRate) { this.challengeWinRate = challengeWinRate; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public double getConsistencyScore() { return consistencyScore; }
    public void setConsistencyScore(double consistencyScore) { this.consistencyScore = consistencyScore; }

    public double getAverageDifficulty() { return averageDifficulty; }
    public void setAverageDifficulty(double averageDifficulty) { this.averageDifficulty = averageDifficulty; }

    public double getMaxDifficulty() { return maxDifficulty; }
    public void setMaxDifficulty(double maxDifficulty) { this.maxDifficulty = maxDifficulty; }

    public long getTotalTrainingTime() { return totalTrainingTime; }
    public void setTotalTrainingTime(long totalTrainingTime) { this.totalTrainingTime = totalTrainingTime; }

    public double getAverageTrainingTime() { return averageTrainingTime; }
    public void setAverageTrainingTime(double averageTrainingTime) { this.averageTrainingTime = averageTrainingTime; }

    public LocalDateTime getLastTrainingDate() { return lastTrainingDate; }
    public void setLastTrainingDate(LocalDateTime lastTrainingDate) { this.lastTrainingDate = lastTrainingDate; }

    public boolean isHasTrainedToday() { return hasTrainedToday; }
    public void setHasTrainedToday(boolean hasTrainedToday) { this.hasTrainedToday = hasTrainedToday; }

    public int getDaysActive() { return daysActive; }
    public void setDaysActive(int daysActive) { this.daysActive = daysActive; }

    public int getOverallRank() { return overallRank; }
    public void setOverallRank(int overallRank) { this.overallRank = overallRank; }

    public int getMonthlyRank() { return monthlyRank; }
    public void setMonthlyRank(int monthlyRank) { this.monthlyRank = monthlyRank; }

    public int getStreakRank() { return streakRank; }
    public void setStreakRank(int streakRank) { this.streakRank = streakRank; }

    public int getChallengeRank() { return challengeRank; }
    public void setChallengeRank(int challengeRank) { this.challengeRank = challengeRank; }

    public int getConsistencyRank() { return consistencyRank; }
    public void setConsistencyRank(int consistencyRank) { this.consistencyRank = consistencyRank; }

    // Helper Methods
    public boolean isActive() {
        return hasTrainedToday || currentStreak > 0;
    }

    public String getActivityLevel() {
        if (currentStreak >= 7) return "Sehr Aktiv";
        if (currentStreak >= 3) return "Aktiv";
        if (hasTrainedToday) return "Heute Aktiv";
        return "Inaktiv";
    }

    public String getPerformanceLevel() {
        if (averageDifficulty >= 4.0) return "Expert";
        if (averageDifficulty >= 3.0) return "Fortgeschritten";
        if (averageDifficulty >= 2.0) return "Anfänger";
        return "Neuling";
    }
}