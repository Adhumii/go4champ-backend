package com.go4champ.go4champ.model;

import jakarta.persistence.*;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "game_id", nullable = false)
    private int gameId;
    private int points;
    //userId oder so muss noch übergeben wereden
    private String badge;
    private int ranglist;

    @OneToOne(mappedBy = "game")
    private User user;

    public Game() {
    }

    public Game(int gameId, int points, User userId, String badge, int ranglist) {
        this.gameId = gameId;
        this.badge = badge;
        this.points = points;
        this.ranglist = ranglist;
        this.user = userId;
    }
    public Game(int points, String badge, int ranglist) {
        this.points = points;
        this.badge = badge;
        this.ranglist = ranglist;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        // Vermeiden von endlosen Rekursionen bei bidirektionalen Beziehungen
        if (user == this.user) {
            return;
        }

        // Alte Beziehung entfernen
        User oldUser = this.user;
        this.user = null;

        if (oldUser != null && oldUser.getGame() == this) {
            oldUser.setGame(null);
        }

        // Neue Beziehung setzen
        this.user = user;

        // User-Seitige Beziehung aufbauen
        if (user != null && user.getGame() != this) {
            user.setGame(this);
        }
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public int getRanglist() {
        return ranglist;
    }

    public void setRanglist(int ranglist) {
        this.ranglist = ranglist;
    }
}