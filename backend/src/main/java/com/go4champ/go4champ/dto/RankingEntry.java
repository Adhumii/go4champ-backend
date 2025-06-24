package com.go4champ.go4champ.dto;

/**
 * DTO für einen einzelnen Ranking-Eintrag
 */
public class RankingEntry {

    private int position;
    private String username;
    private String name;
    private String avatarId;
    private long value; // z.B. Anzahl Trainings, Streak-Länge, etc.
    private String valueDescription; // z.B. "42 Trainings", "7 Tage Streak"
    private double score; // Numerischer Score für genaue Bewertung
    private String badge; // z.B. "🥇", "🔥", "💪"
    private boolean isCurrentUser;

    // Zusätzliche Details
    private String additionalInfo; // z.B. "Seit 3 Tagen aktiv"
    private double changeFromLastPeriod; // +/- Veränderung zum letzten Zeitraum

    // Constructors
    public RankingEntry() {}

    public RankingEntry(int position, String username, String name, long value, String valueDescription) {
        this.position = position;
        this.username = username;
        this.name = name;
        this.value = value;
        this.valueDescription = valueDescription;
        this.score = value; // Default: value = score
    }

    // Getters and Setters
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }

    public String getValueDescription() { return valueDescription; }
    public void setValueDescription(String valueDescription) { this.valueDescription = valueDescription; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public boolean isCurrentUser() { return isCurrentUser; }
    public void setCurrentUser(boolean currentUser) { isCurrentUser = currentUser; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public double getChangeFromLastPeriod() { return changeFromLastPeriod; }
    public void setChangeFromLastPeriod(double changeFromLastPeriod) { this.changeFromLastPeriod = changeFromLastPeriod; }
}