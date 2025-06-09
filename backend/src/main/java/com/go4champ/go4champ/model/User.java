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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Training> trainings = new ArrayList<>();

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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        // Alte Beziehung entfernen
        if (this.game != null) {
            this.game.setUser(null);
        }

        // Neue Beziehung setzen
        this.game = game;

        // Rückreferenz setzen
        if (game != null && game.getUser() != this) {
            game.setUser(this);
        }
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
                ", avatarID='" + avatarID + '\'' +
                '}';
    }
}