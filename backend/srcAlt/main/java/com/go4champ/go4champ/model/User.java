package com.go4champ.go4champ.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Primary;

@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @Column(unique = true, nullable = false)
    private String username ;

    private String password;

    private String name;

    private int age;

    private boolean gender;

    private int weight;

    private int height;

    private int weightGoal;



    public void setHeight(int height) {
        this.height = height;
    }
//private Traingeinheit vorliebe muss als Object in einem array gespeichert werden und ist Optinal


    private String avatarID;
    //leerer Konstruktor für Hibernate
    public User() {

    };

    public User(String username,String password, String name, int age, boolean gender, int weight, int weightGoal, String allergies, String sickness, String avatarID) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.weightGoal = weightGoal;
        this.avatarID = avatarID;
    }
    public String getUsername() {
        return username;
    }
    public int getHeight() {
        return height;
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

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
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
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", weight=" + weight +
                ", weightGoal=" + weightGoal +
                ", avatarID='" + avatarID + '\'' +
                '}';
    }

}