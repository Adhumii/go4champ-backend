package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepo extends JpaRepository<FriendRequest, Long> {

    // Findet alle eingehenden Freundschaftsanfragen für einen User
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.receiver.username = :username AND fr.status = 'PENDING'")
    List<FriendRequest> findIncomingRequests(@Param("username") String username);

    // Findet alle ausgehenden Freundschaftsanfragen für einen User
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.sender.username = :username AND fr.status = 'PENDING'")
    List<FriendRequest> findOutgoingRequests(@Param("username") String username);

    // Findet alle Freundschaftsanfragen für einen User (eingehend und ausgehend)
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "(fr.receiver.username = :username OR fr.sender.username = :username) AND fr.status = 'PENDING'")
    List<FriendRequest> findAllPendingRequests(@Param("username") String username);

    // Prüft ob bereits eine Anfrage zwischen zwei Usern existiert
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "((fr.sender.username = :sender AND fr.receiver.username = :receiver) OR " +
            "(fr.sender.username = :receiver AND fr.receiver.username = :sender)) AND " +
            "fr.status = 'PENDING'")
    Optional<FriendRequest> findPendingRequestBetweenUsers(@Param("sender") String sender, @Param("receiver") String receiver);

    // Findet eine spezifische Anfrage zwischen zwei Usern
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "fr.sender.username = :sender AND fr.receiver.username = :receiver AND fr.status = 'PENDING'")
    Optional<FriendRequest> findPendingRequestFromTo(@Param("sender") String sender, @Param("receiver") String receiver);

    // Findet alle Anfragen zwischen zwei Usern (unabhängig vom Status)
    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "(fr.sender.username = :username1 AND fr.receiver.username = :username2) OR " +
            "(fr.sender.username = :username2 AND fr.receiver.username = :username1)")
    List<FriendRequest> findAllRequestsBetweenUsers(@Param("username1") String username1, @Param("username2") String username2);

    // Zählt eingehende Anfragen
    @Query("SELECT COUNT(fr) FROM FriendRequest fr WHERE fr.receiver.username = :username AND fr.status = 'PENDING'")
    long countIncomingRequests(@Param("username") String username);

    // Zählt ausgehende Anfragen
    @Query("SELECT COUNT(fr) FROM FriendRequest fr WHERE fr.sender.username = :username AND fr.status = 'PENDING'")
    long countOutgoingRequests(@Param("username") String username);
}