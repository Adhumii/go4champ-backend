package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.Friendship;
import com.go4champ.go4champ.model.FriendRequest;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.FriendshipRepo;
import com.go4champ.go4champ.repo.FriendRequestRepo;
import com.go4champ.go4champ.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FriendshipService {

    @Autowired
    private FriendshipRepo friendshipRepo;

    @Autowired
    private FriendRequestRepo friendRequestRepo;

    @Autowired
    private UserRepo userRepo;

    // =============================================================================
    // FREUNDSCHAFTEN VERWALTEN
    // =============================================================================

    /**
     * Holt alle Freunde eines Users
     */
    public List<User> getFriends(String username) {
        List<Friendship> friendships = friendshipRepo.findAllByUsername(username);
        return friendships.stream()
                .map(friendship -> friendship.getFriend(username))
                .filter(friend -> friend != null)
                .toList();
    }

    /**
     * Prüft ob zwei User Freunde sind
     */
    public boolean areFriends(String username1, String username2) {
        return friendshipRepo.areFriends(username1, username2);
    }

    /**
     * Zählt die Anzahl der Freunde eines Users
     */
    public long getFriendCount(String username) {
        return friendshipRepo.countFriendsByUsername(username);
    }

    /**
     * Entfernt eine Freundschaft
     */
    public boolean removeFriend(String username1, String username2) {
        Optional<Friendship> friendship = friendshipRepo.findByUsernames(username1, username2);
        if (friendship.isPresent()) {
            friendshipRepo.delete(friendship.get());
            return true;
        }
        return false;
    }

    // =============================================================================
    // FREUNDSCHAFTSANFRAGEN VERWALTEN
    // =============================================================================

    /**
     * Sendet eine Freundschaftsanfrage
     */
    public String sendFriendRequest(String senderUsername, String receiverUsername, String message) {
        // Prüfungen
        if (senderUsername.equals(receiverUsername)) {
            return "Du kannst dir nicht selbst eine Freundschaftsanfrage senden";
        }

        // User existieren?
        Optional<User> sender = userRepo.findById(senderUsername);
        Optional<User> receiver = userRepo.findById(receiverUsername);

        if (sender.isEmpty()) {
            return "Sender nicht gefunden";
        }
        if (receiver.isEmpty()) {
            return "Empfänger nicht gefunden";
        }

        // Bereits Freunde?
        if (areFriends(senderUsername, receiverUsername)) {
            return "Ihr seid bereits Freunde";
        }

        // Bereits eine Anfrage vorhanden?
        Optional<FriendRequest> existingRequest = friendRequestRepo.findPendingRequestBetweenUsers(senderUsername, receiverUsername);
        if (existingRequest.isPresent()) {
            return "Es existiert bereits eine Freundschaftsanfrage zwischen euch";
        }

        // Neue Anfrage erstellen
        FriendRequest request = new FriendRequest(sender.get(), receiver.get(), message);
        friendRequestRepo.save(request);

        return "Freundschaftsanfrage gesendet";
    }

    /**
     * Akzeptiert eine Freundschaftsanfrage
     */
    public String acceptFriendRequest(Long requestId, String username) {
        Optional<FriendRequest> requestOpt = friendRequestRepo.findById(requestId);
        if (requestOpt.isEmpty()) {
            return "Freundschaftsanfrage nicht gefunden";
        }

        FriendRequest request = requestOpt.get();

        // Prüfen ob der User der Empfänger ist
        if (!request.getReceiver().getUsername().equals(username)) {
            return "Du bist nicht berechtigt, diese Anfrage zu akzeptieren";
        }

        if (!request.isPending()) {
            return "Diese Anfrage wurde bereits bearbeitet";
        }

        // Anfrage akzeptieren
        request.accept();
        friendRequestRepo.save(request);

        // Freundschaft erstellen
        Friendship friendship = new Friendship(request.getSender(), request.getReceiver());
        friendshipRepo.save(friendship);

        return "Freundschaftsanfrage akzeptiert";
    }

    /**
     * Lehnt eine Freundschaftsanfrage ab
     */
    public String rejectFriendRequest(Long requestId, String username) {
        Optional<FriendRequest> requestOpt = friendRequestRepo.findById(requestId);
        if (requestOpt.isEmpty()) {
            return "Freundschaftsanfrage nicht gefunden";
        }

        FriendRequest request = requestOpt.get();

        // Prüfen ob der User der Empfänger ist
        if (!request.getReceiver().getUsername().equals(username)) {
            return "Du bist nicht berechtigt, diese Anfrage abzulehnen";
        }

        if (!request.isPending()) {
            return "Diese Anfrage wurde bereits bearbeitet";
        }

        // Anfrage ablehnen
        request.reject();
        friendRequestRepo.save(request);

        return "Freundschaftsanfrage abgelehnt";
    }

    /**
     * Storniert eine gesendete Freundschaftsanfrage
     */
    public String cancelFriendRequest(Long requestId, String username) {
        Optional<FriendRequest> requestOpt = friendRequestRepo.findById(requestId);
        if (requestOpt.isEmpty()) {
            return "Freundschaftsanfrage nicht gefunden";
        }

        FriendRequest request = requestOpt.get();

        // Prüfen ob der User der Sender ist
        if (!request.getSender().getUsername().equals(username)) {
            return "Du bist nicht berechtigt, diese Anfrage zu stornieren";
        }

        if (!request.isPending()) {
            return "Diese Anfrage kann nicht mehr storniert werden";
        }

        // Anfrage stornieren
        request.cancel();
        friendRequestRepo.save(request);

        return "Freundschaftsanfrage storniert";
    }

    /**
     * Holt alle eingehenden Freundschaftsanfragen
     */
    public List<FriendRequest> getIncomingRequests(String username) {
        return friendRequestRepo.findIncomingRequests(username);
    }

    /**
     * Holt alle ausgehenden Freundschaftsanfragen
     */
    public List<FriendRequest> getOutgoingRequests(String username) {
        return friendRequestRepo.findOutgoingRequests(username);
    }

    /**
     * Holt alle ausstehenden Freundschaftsanfragen (eingehend und ausgehend)
     */
    public List<FriendRequest> getAllPendingRequests(String username) {
        return friendRequestRepo.findAllPendingRequests(username);
    }

    /**
     * Zählt eingehende Freundschaftsanfragen
     */
    public long getIncomingRequestCount(String username) {
        return friendRequestRepo.countIncomingRequests(username);
    }

    /**
     * Zählt ausgehende Freundschaftsanfragen
     */
    public long getOutgoingRequestCount(String username) {
        return friendRequestRepo.countOutgoingRequests(username);
    }

    // =============================================================================
    // HILFSMETHODEN
    // =============================================================================

    /**
     * Prüft den Freundschaftsstatus zwischen zwei Usern
     */
    public String getFriendshipStatus(String username1, String username2) {
        // Wichtig: Case-insensitive Vergleich!
        if (areFriends(username1.toLowerCase(), username2.toLowerCase())) {
            return "FRIENDS";
        }

        Optional<FriendRequest> request = friendRequestRepo.findPendingRequestBetweenUsers(username1, username2);
        if (request.isPresent()) {
            if (request.get().getSender().getUsername().equalsIgnoreCase(username1)) {
                return "REQUEST_SENT";
            } else {
                return "REQUEST_RECEIVED";
            }
        }

        return "NOT_FRIENDS";
    }
}