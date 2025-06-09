package com.go4champ.go4champ.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Training {
    @Id
    private int TraingID;

    private String Titel;

    private String Description;

    private float difficulty;
    //in oder outdoor
    private boolean Typ;
    //vielleicht float
    private int duration;
    //Ziel gruppe was ist damit gemeint muss noch eingefügt werden?


    public Training() {

    }

    public Training(int traingID, int duration, float difficulty, boolean typ, String description, String titel) {
        this.TraingID = traingID;
        this.duration = duration;
        this.difficulty = difficulty;
        this.Typ = typ;
        this.Description = description;
        this.Titel = titel;
    }

    public int getTraingID() {
        return TraingID;
    }

    public void setTraingID(int traingID) {
        TraingID = traingID;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isTyp() {
        return Typ;
    }

    public void setTyp(boolean typ) {
        Typ = typ;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getTitel() {
        return Titel;
    }

    public void setTitel(String titel) {
        Titel = titel;
    }
}