package com.go4champ.go4champ.dto;

/**
 * DTO für Challenge Übersicht/Statistiken
 */
public class ChallengeOverviewResponse {

    private long totalChallenges;
    private long wonChallenges;
    private long lostChallenges;
    private long activeChallenges;
    private long pendingIncoming;
    private long pendingOutgoing;
    private double winRate; // Gewinnrate in Prozent

    // Challenges nach Type
    private long repsWon;
    private long weightWon;
    private long freeWon;

    private long repsLost;
    private long weightLost;
    private long freeLost;

    // Constructors
    public ChallengeOverviewResponse() {}

    public ChallengeOverviewResponse(long totalChallenges, long wonChallenges, long lostChallenges,
                                     long activeChallenges, long pendingIncoming, long pendingOutgoing,
                                     double winRate, long repsWon, long weightWon, long freeWon,
                                     long repsLost, long weightLost, long freeLost) {
        this.totalChallenges = totalChallenges;
        this.wonChallenges = wonChallenges;
        this.lostChallenges = lostChallenges;
        this.activeChallenges = activeChallenges;
        this.pendingIncoming = pendingIncoming;
        this.pendingOutgoing = pendingOutgoing;
        this.winRate = winRate;
        this.repsWon = repsWon;
        this.weightWon = weightWon;
        this.freeWon = freeWon;
        this.repsLost = repsLost;
        this.weightLost = weightLost;
        this.freeLost = freeLost;
    }

    // Getters and Setters
    public long getTotalChallenges() { return totalChallenges; }
    public void setTotalChallenges(long totalChallenges) { this.totalChallenges = totalChallenges; }

    public long getWonChallenges() { return wonChallenges; }
    public void setWonChallenges(long wonChallenges) { this.wonChallenges = wonChallenges; }

    public long getLostChallenges() { return lostChallenges; }
    public void setLostChallenges(long lostChallenges) { this.lostChallenges = lostChallenges; }

    public long getActiveChallenges() { return activeChallenges; }
    public void setActiveChallenges(long activeChallenges) { this.activeChallenges = activeChallenges; }

    public long getPendingIncoming() { return pendingIncoming; }
    public void setPendingIncoming(long pendingIncoming) { this.pendingIncoming = pendingIncoming; }

    public long getPendingOutgoing() { return pendingOutgoing; }
    public void setPendingOutgoing(long pendingOutgoing) { this.pendingOutgoing = pendingOutgoing; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public long getRepsWon() { return repsWon; }
    public void setRepsWon(long repsWon) { this.repsWon = repsWon; }

    public long getWeightWon() { return weightWon; }
    public void setWeightWon(long weightWon) { this.weightWon = weightWon; }

    public long getFreeWon() { return freeWon; }
    public void setFreeWon(long freeWon) { this.freeWon = freeWon; }

    public long getRepsLost() { return repsLost; }
    public void setRepsLost(long repsLost) { this.repsLost = repsLost; }

    public long getWeightLost() { return weightLost; }
    public void setWeightLost(long weightLost) { this.weightLost = weightLost; }

    public long getFreeLost() { return freeLost; }
    public void setFreeLost(long freeLost) { this.freeLost = freeLost; }
}