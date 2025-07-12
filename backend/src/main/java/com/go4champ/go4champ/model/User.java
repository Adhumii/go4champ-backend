package com.go4champ.go4champ.model;

import jakarta.persistence.*;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @Column(unique = true, nullable = false)
    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    private String password;

    private String email;

    // NEU für E-Mail Verification
    private boolean emailVerified = false;
    private String verificationToken;

    private String name;

    private int age;

    private String gender;

    private int weight;

    private int height;

    private int weightGoal;

    // NEU: Equipment Liste
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_equipment", joinColumns = @JoinColumn(name = "username"))
    @Column(name = "equipment")
    private List<String> availableEquipment = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Training> trainings = new ArrayList<>();

    // NEU: TrainingsPlan-Beziehung
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TrainingsPlan> trainingPlans = new ArrayList<>();

    private String avatarID;

    //leerer Konstruktor für Hibernate
    public User() {

    }

    public User(String name) {
        this.name = name;
    }

    public User(String username, String password, String name, int age, String gender, int weight, int weightGoal, String allergies, String sickness, String avatarID) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.weightGoal = weightGoal;
        this.avatarID = avatarID;
    }

    // NEU: Konstruktor mit Email
    public User(String username, String password, String email, String name, int age, String gender, int weight, int weightGoal, String allergies, String sickness, String avatarID) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.weightGoal = weightGoal;
        this.avatarID = avatarID;
    }

    // NEU: Equipment Getter und Setter
    public List<String> getAvailableEquipment() {
        return availableEquipment;
    }

    public void setAvailableEquipment(List<String> availableEquipment) {
        this.availableEquipment = availableEquipment;
    }

    // NEU: Equipment Helper Methoden
    public void addEquipment(String equipment) {
        if (!availableEquipment.contains(equipment)) {
            availableEquipment.add(equipment);
        }
    }

    public void removeEquipment(String equipment) {
        availableEquipment.remove(equipment);
    }

    public boolean hasEquipment(String equipment) {
        return availableEquipment.contains(equipment);
    }

    // NEU: Equipment Check Methoden für spezielle Geräte
    public boolean hasJumpRope() {
        return hasEquipment("JUMP_ROPE");
    }

    public boolean hasPullUpBar() {
        return hasEquipment("PULL_UP_BAR");
    }

    public boolean hasKettlebell() {
        return hasEquipment("KETTLEBELL");
    }

    public boolean hasResistanceBand() {
        return hasEquipment("RESISTANCE_BAND");
    }

    public boolean hasDumbbells() {
        return hasEquipment("DUMBBELLS");
    }

    public boolean hasMat() {
        return hasEquipment("MAT");
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setHeight(int height) {
        this.height = height;
    }



    public List<Training> getTrainings() {
        return trainings;
    }

    public void addTraining(Training training) {
        trainings.add(training);
        training.setUser(this);
    }

    public void removeTraining(Training training) {
        trainings.remove(training);
        training.setUser(null);
    }

    // NEU: TrainingsPlan Getter und Setter
    public List<TrainingsPlan> getTrainingPlans() {
        return trainingPlans;
    }

    public void setTrainingPlans(List<TrainingsPlan> trainingPlans) {
        this.trainingPlans = trainingPlans;
    }

    public void addTrainingPlan(TrainingsPlan trainingPlan) {
        trainingPlans.add(trainingPlan);
        trainingPlan.setUser(this);
    }

    public void removeTrainingPlan(TrainingsPlan trainingPlan) {
        trainingPlans.remove(trainingPlan);
        trainingPlan.setUser(null);
    }

    public String getUsername() {
        return username;
    }

    public int getHeight() {
        return height;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // NEU: E-Mail Verification Getter und Setter
    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeightGoal() {
        return weightGoal;
    }

    public void setWeightGoal(int weightGoal) {
        this.weightGoal = weightGoal;
    }

    public String getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(String avatarID) {
        this.avatarID = avatarID;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", weight=" + weight +
                ", weightGoal=" + weightGoal +
                ", availableEquipment=" + availableEquipment +
                ", avatarID='" + avatarID + '\'' +
                '}';
    }
}