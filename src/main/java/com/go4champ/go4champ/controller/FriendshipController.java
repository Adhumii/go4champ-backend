package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.FriendRequest;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.security.JwtTokenUtil;
import com.go4champ.go4champ.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "FriendshipController", description = "API für Freundschaften und Freundschaftsanfragen")
@RestController
@RequestMapping("/api")
public class FriendshipController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private FriendshipService friendshipService;

    // =============================================================================
    // FREUNDE VERWALTEN
    // =============================================================================

    @Operation(summary = "Holt alle Freunde des eingeloggten Users")
    @GetMapping("/me/friends")
    public ResponseEntity<?> getMyFriends(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Freunde holen
            List<User> friends = friendshipService.getFriends(username);
            return ResponseEntity.ok(Map.of(
                    "friends", friends,
                    "count", friends.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der Freunde: " + e.getMessage());
        }
    }

    @Operation(summary = "Prüft den Freundschaftsstatus mit einem anderen User")
    @GetMapping("/me/friendship-status/{otherUsername}")
    public ResponseEntity<?> getFriendshipStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String otherUsername) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Status prüfen
            String status = friendshipService.getFriendshipStatus(username, otherUsername);
            boolean areFriends = status.equals("FRIENDS");

            return ResponseEntity.ok(Map.of(
                    "status", status,
                    "areFriends", areFriends,
                    "otherUser", otherUsername
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Prüfen des Freundschaftsstatus: " + e.getMessage());
        }
    }

    @Operation(summary = "Entfernt einen Freund")
    @DeleteMapping("/me/friends/{friendUsername}")
    public ResponseEntity<?> removeFriend(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String friendUsername) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Freundschaft entfernen
            boolean removed = friendshipService.removeFriend(username, friendUsername);
            if (removed) {
                return ResponseEntity.ok(Map.of(
                        "message", "Freund erfolgreich entfernt",
                        "removedFriend", friendUsername
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Freundschaft nicht gefunden");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Entfernen des Freundes: " + e.getMessage());
        }
    }

    // =============================================================================
    // FREUNDSCHAFTSANFRAGEN SENDEN
    // =============================================================================

    @Operation(summary = "Sendet eine Freundschaftsanfrage")
    @PostMapping("/me/friend-requests")
    public ResponseEntity<?> sendFriendRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> requestData) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            String receiverUsername = requestData.get("receiverUsername");
            String message = requestData.get("message");

            if (receiverUsername == null || receiverUsername.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Empfänger-Username fehlt");
            }

            // Freundschaftsanfrage senden
            String result = friendshipService.sendFriendRequest(username, receiverUsername, message);

            if (result.equals("Freundschaftsanfrage gesendet")) {
                return ResponseEntity.ok(Map.of("message", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Senden der Freundschaftsanfrage: " + e.getMessage());
        }
    }

    // =============================================================================
    // EINGEHENDE FREUNDSCHAFTSANFRAGEN
    // =============================================================================

    @Operation(summary = "Holt alle eingehenden Freundschaftsanfragen")
    @GetMapping("/me/friend-requests/incoming")
    public ResponseEntity<?> getIncomingFriendRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Eingehende Anfragen holen
            List<FriendRequest> incomingRequests = friendshipService.getIncomingRequests(username);
            long count = friendshipService.getIncomingRequestCount(username);

            return ResponseEntity.ok(Map.of(
                    "requests", incomingRequests,
                    "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der eingehenden Anfragen: " + e.getMessage());
        }
    }

    @Operation(summary = "Akzeptiert eine Freundschaftsanfrage")
    @PostMapping("/me/friend-requests/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Anfrage akzeptieren
            String result = friendshipService.acceptFriendRequest(requestId, username);

            if (result.equals("Freundschaftsanfrage akzeptiert")) {
                return ResponseEntity.ok(Map.of("message", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Akzeptieren der Anfrage: " + e.getMessage());
        }
    }

    @Operation(summary = "Lehnt eine Freundschaftsanfrage ab")
    @PostMapping("/me/friend-requests/{requestId}/reject")
    public ResponseEntity<?> rejectFriendRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Anfrage ablehnen
            String result = friendshipService.rejectFriendRequest(requestId, username);

            if (result.equals("Freundschaftsanfrage abgelehnt")) {
                return ResponseEntity.ok(Map.of("message", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Ablehnen der Anfrage: " + e.getMessage());
        }
    }

    // =============================================================================
    // AUSGEHENDE FREUNDSCHAFTSANFRAGEN
    // =============================================================================

    @Operation(summary = "Holt alle ausgehenden Freundschaftsanfragen")
    @GetMapping("/me/friend-requests/outgoing")
    public ResponseEntity<?> getOutgoingFriendRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Ausgehende Anfragen holen
            List<FriendRequest> outgoingRequests = friendshipService.getOutgoingRequests(username);
            long count = friendshipService.getOutgoingRequestCount(username);

            return ResponseEntity.ok(Map.of(
                    "requests", outgoingRequests,
                    "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der ausgehenden Anfragen: " + e.getMessage());
        }
    }

    @Operation(summary = "Storniert eine gesendete Freundschaftsanfrage")
    @DeleteMapping("/me/friend-requests/{requestId}")
    public ResponseEntity<?> cancelFriendRequest(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long requestId) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Anfrage stornieren
            String result = friendshipService.cancelFriendRequest(requestId, username);

            if (result.equals("Freundschaftsanfrage storniert")) {
                return ResponseEntity.ok(Map.of("message", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Stornieren der Anfrage: " + e.getMessage());
        }
    }

    // =============================================================================
    // ÜBERSICHT UND STATISTIKEN
    // =============================================================================

    @Operation(summary = "Holt eine Übersicht über Freunde und Anfragen")
    @GetMapping("/me/friendship-overview")
    public ResponseEntity<?> getFriendshipOverview(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Statistiken sammeln
            long friendCount = friendshipService.getFriendCount(username);
            long incomingRequestCount = friendshipService.getIncomingRequestCount(username);
            long outgoingRequestCount = friendshipService.getOutgoingRequestCount(username);

            return ResponseEntity.ok(Map.of(
                    "friendCount", friendCount,
                    "incomingRequestCount", incomingRequestCount,
                    "outgoingRequestCount", outgoingRequestCount,
                    "totalPendingRequests", incomingRequestCount + outgoingRequestCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen der Freundschaftsübersicht: " + e.getMessage());
        }
    }

    @Operation(summary = "Holt alle ausstehenden Freundschaftsanfragen (eingehend und ausgehend)")
    @GetMapping("/me/friend-requests/all")
    public ResponseEntity<?> getAllPendingRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            // JWT Token aus Header extrahieren
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein gültiger Token");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            // Alle ausstehenden Anfragen holen
            List<FriendRequest> incomingRequests = friendshipService.getIncomingRequests(username);
            List<FriendRequest> outgoingRequests = friendshipService.getOutgoingRequests(username);

            return ResponseEntity.ok(Map.of(
                    "incoming", Map.of(
                            "requests", incomingRequests,
                            "count", incomingRequests.size()
                    ),
                    "outgoing", Map.of(
                            "requests", outgoingRequests,
                            "count", outgoingRequests.size()
                    ),
                    "totalCount", incomingRequests.size() + outgoingRequests.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Abrufen aller Anfragen: " + e.getMessage());
        }
    }
}