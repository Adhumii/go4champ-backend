package com.go4champ.go4champ.model;

/**
 * Challenge Status
 */
public enum ChallengeStatus {
    PENDING,    // Wartet auf Annahme
    ACCEPTED,   // Angenommen, läuft
    COMPLETED,  // Abgeschlossen
    CANCELLED,  // Abgebrochen
    EXPIRED     // Abgelaufen
}