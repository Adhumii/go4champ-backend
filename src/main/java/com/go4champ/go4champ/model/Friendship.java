package com.go4champ.go4champ.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "friendship")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user1_username", nullable = false)
    private User user1;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user2_username", nullable = false)
    private User user2;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Konstruktoren
    public Friendship() {
        this.createdAt = LocalDateTime.now();
    }

    public Friendship(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = LocalDateTime.now();
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper-Methoden
    public User getFriend(String username) {
        if (user1.getUsername().equals(username)) {
            return user2;
        } else if (user2.getUsername().equals(username)) {
            return user1;
        }
        return null;
    }

    public boolean involves(String username) {
        return user1.getUsername().equals(username) || user2.getUsername().equals(username);
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "id=" + id +
                ", user1='" + user1.getUsername() + '\'' +
                ", user2='" + user2.getUsername() + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}