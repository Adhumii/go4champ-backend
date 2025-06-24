package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.Friendship;
import com.go4champ.go4champ.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

    // Findet alle Freundschaften eines Users
    @Query("SELECT f FROM Friendship f WHERE f.user1.username = :username OR f.user2.username = :username")
    List<Friendship> findAllByUsername(@Param("username") String username);

    // Prüft ob zwei User bereits Freunde sind
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user1.username = :username1 AND f.user2.username = :username2) OR " +
            "(f.user1.username = :username2 AND f.user2.username = :username1)")
    Optional<Friendship> findByUsernames(@Param("username1") String username1, @Param("username2") String username2);

    // Findet alle Freunde eines Users als User-Objekte - vereinfachte Query
    @Query("SELECT f.user2 FROM Friendship f WHERE f.user1.username = :username " +
            "UNION " +
            "SELECT f.user1 FROM Friendship f WHERE f.user2.username = :username")
    List<User> findFriendsByUsername(@Param("username") String username);

    // Zählt die Anzahl der Freunde eines Users
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.user1.username = :username OR f.user2.username = :username")
    long countFriendsByUsername(@Param("username") String username);

    // Prüft ob zwei User Freunde sind (boolean)
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "(f.user1.username = :username1 AND f.user2.username = :username2) OR " +
            "(f.user1.username = :username2 AND f.user2.username = :username1)")
    boolean areFriends(@Param("username1") String username1, @Param("username2") String username2);
}