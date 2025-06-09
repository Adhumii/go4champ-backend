package com.go4champ.go4champ.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"trainings_plan\"")
public class TrainingsPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int planId;

    private String planName;

    private String description;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "training_plan_trainings",
            joinColumns = @JoinColumn(name = "plan_id"),
            inverseJoinColumns = @JoinColumn(name = "training_id")
    )
    private List<Training> trainings = new ArrayList<>();

    // Konstruktoren
    public TrainingsPlan() {
    }

    public TrainingsPlan(String planName, String description, User user) {
        this.planName = planName;
        this.description = description;
        this.user = user;
        this.trainings = new ArrayList<>();
    }

    // Hilfsmethoden für Training-Management
    public void addTraining(Training training) {
        if (trainings == null) {
            trainings = new ArrayList<>();
        }
        trainings.add(training);
    }

    public void removeTraining(Training training) {
        if (trainings != null) {
            trainings.remove(training);
        }
    }

    public void removeTrainingById(int trainingId) {
        if (trainings != null) {
            trainings.removeIf(training -> training.getTrainingId() == trainingId);
        }
    }

    // Getters und Setters
    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
    }
}