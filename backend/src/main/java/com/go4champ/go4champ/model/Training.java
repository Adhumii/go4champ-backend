package com.go4champ.go4champ.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "\"training\"")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int trainingId;

    private String title;

    private String description;

    @ManyToOne
    @JsonIgnore

    private User user;

    private float difficulty;

    // true = indoor, false = outdoor (or use enum for better clarity)
    private boolean type;

    private int duration;

    //  Define targetGroup field once the requirement is clarified

    public Training() {
    }

    public Training(int trainingId, int duration, float difficulty, boolean type, String description, String title) {
        this.trainingId = trainingId;
        this.duration = duration;
        this.difficulty = difficulty;
        this.type = type;
        this.description = description;
        this.title = title;
    }

    public Training(String title, User user) {
        this.title = title;
        this.user = user;
    }

    // Getters and Setters
    public int getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(int trainingId) {
        this.trainingId = trainingId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
