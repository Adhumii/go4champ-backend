package com.go4champ.go4champ.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Challenge Entity - Herausforderungen zwischen Freunden
 */
@Entity
@Table(name = "challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "challenger_username", referencedColumnName = "username")
    private User challenger; // Der Herausforderer

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "challenged_username", referencedColumnName = "username")
    private User challenged; // Der Herausgeforderte

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeType type; // REPS, WEIGHT, FREE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status; // PENDING, ACCEPTED, COMPLETED, CANCELLED

    @Column(nullable = false, length = 200)
    private String title; // z.B. "Liegestütz Challenge"

    @Column(length = 500)
    private String description; // Beschreibung der Challenge

    // Ziel-Werte (je nach Type)
    private Double targetValue; // Für REPS und WEIGHT
    private String targetUnit; // z.B. "Wiederholungen", "kg", etc.

    // Ergebnisse
    private Double challengerResult; // Ergebnis des Herausforderers
    private Double challengedResult; // Ergebnis des Herausgeforderten

    // Für FREE Type - Sieger-Bestimmung
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_username", referencedColumnName = "username")
    private User winner; // Nur bei FREE Type relevant

    @Column(name = "challenger_submitted")
    private boolean challengerSubmitted = false; // Hat der Herausforderer sein Ergebnis eingegeben?

    @Column(name = "challenged_submitted")
    private boolean challengedSubmitted = false; // Hat der Herausgeforderte sein Ergebnis eingegeben?

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline; // Bis wann die Challenge abgeschlossen sein muss

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.deadline == null) {
            // Standard: 7 Tage Zeit
            this.deadline = this.createdAt.plusDays(7);
        }
    }

    /**
     * Prüft ob die Challenge abgeschlossen ist
     */
    public boolean isCompleted() {
        if (type == ChallengeType.FREE) {
            // Bei FREE Type: Beide müssen eingeben + Sieger bestimmt
            return challengerSubmitted && challengedSubmitted && winner != null;
        } else {
            // Bei REPS/WEIGHT: Beide müssen ihre Werte eingeben
            return challengerSubmitted && challengedSubmitted;
        }
    }

    /**
     * Bestimmt den Sieger bei REPS und WEIGHT Challenges
     */
    public User determineWinner() {
        if (type == ChallengeType.FREE) {
            return winner; // Manuell bestimmt
        }

        if (challengerResult == null || challengedResult == null) {
            return null; // Noch nicht beide eingegeben
        }

        // Bei REPS und WEIGHT: Höherer Wert gewinnt
        if (challengerResult > challengedResult) {
            return challenger;
        } else if (challengedResult > challengerResult) {
            return challenged;
        } else {
            return null; // Unentschieden
        }
    }

    /**
     * Prüft ob User an dieser Challenge beteiligt ist
     */
    public boolean isParticipant(String username) {
        return challenger.getUsername().equals(username) ||
                challenged.getUsername().equals(username);
    }

    /**
     * Gibt den Gegner für einen bestimmten User zurück
     */
    public User getOpponent(String username) {
        if (challenger.getUsername().equals(username)) {
            return challenged;
        } else if (challenged.getUsername().equals(username)) {
            return challenger;
        } else {
            return null;
        }
    }
}