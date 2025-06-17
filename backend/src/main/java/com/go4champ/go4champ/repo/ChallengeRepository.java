package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.Challenge;
import com.go4champ.go4champ.model.ChallengeStatus;
import com.go4champ.go4champ.model.ChallengeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * Findet alle Challenges eines Users (als Herausforderer oder Herausgeforderte)
     */
    @Query("SELECT c FROM Challenge c WHERE c.challenger.username = :username OR c.challenged.username = :username")
    List<Challenge> findAllByUsername(@Param("username") String username);

    /**
     * Findet alle eingehenden Challenges (wo User herausgefordert wurde)
     */
    @Query("SELECT c FROM Challenge c WHERE c.challenged.username = :username")
    List<Challenge> findIncomingChallenges(@Param("username") String username);

    /**
     * Findet alle ausgehenden Challenges (wo User herausgefordert hat)
     */
    @Query("SELECT c FROM Challenge c WHERE c.challenger.username = :username")
    List<Challenge> findOutgoingChallenges(@Param("username") String username);

    /**
     * Findet pending Challenges für einen User
     */
    @Query("SELECT c FROM Challenge c WHERE c.challenged.username = :username AND c.status = 'PENDING'")
    List<Challenge> findPendingChallenges(@Param("username") String username);

    /**
     * Findet aktive Challenges für einen User
     */
    @Query("SELECT c FROM Challenge c WHERE (c.challenger.username = :username OR c.challenged.username = :username) AND c.status = 'ACCEPTED'")
    List<Challenge> findActiveChallenges(@Param("username") String username);

    /**
     * Findet abgeschlossene Challenges für einen User
     */
    @Query("SELECT c FROM Challenge c WHERE (c.challenger.username = :username OR c.challenged.username = :username) AND c.status = 'COMPLETED'")
    List<Challenge> findCompletedChallenges(@Param("username") String username);

    /**
     * Findet Challenges nach Status
     */
    List<Challenge> findByStatus(ChallengeStatus status);

    /**
     * Findet Challenges nach Type
     */
    List<Challenge> findByType(ChallengeType type);

    /**
     * Findet abgelaufene Challenges
     */
    @Query("SELECT c FROM Challenge c WHERE c.deadline < :now AND c.status IN ('PENDING', 'ACCEPTED')")
    List<Challenge> findExpiredChallenges(@Param("now") LocalDateTime now);

    /**
     * Findet Challenge zwischen zwei spezifischen Usern
     */
    @Query("SELECT c FROM Challenge c WHERE " +
            "(c.challenger.username = :user1 AND c.challenged.username = :user2) OR " +
            "(c.challenger.username = :user2 AND c.challenged.username = :user1)")
    List<Challenge> findChallengesBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    /**
     * Zählt gewonnene Challenges eines Users
     */
    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.status = 'COMPLETED' AND " +
            "((c.challenger.username = :username AND c.challengerResult > c.challengedResult) OR " +
            "(c.challenged.username = :username AND c.challengedResult > c.challengerResult) OR " +
            "(c.winner.username = :username AND c.type = 'FREE'))")
    Long countWonChallenges(@Param("username") String username);

    /**
     * Zählt verlorene Challenges eines Users
     */
    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.status = 'COMPLETED' AND " +
            "((c.challenger.username = :username AND c.challengerResult < c.challengedResult) OR " +
            "(c.challenged.username = :username AND c.challengedResult < c.challengerResult) OR " +
            "(c.winner.username != :username AND c.type = 'FREE' AND c.winner IS NOT NULL))")
    Long countLostChallenges(@Param("username") String username);

    /**
     * Findet die letzten N Challenges eines Users
     */
    @Query("SELECT c FROM Challenge c WHERE c.challenger.username = :username OR c.challenged.username = :username " +
            "ORDER BY c.createdAt DESC")
    List<Challenge> findRecentChallenges(@Param("username") String username);

    /**
     * Prüft ob zwischen zwei Usern bereits eine pending Challenge existiert
     */
    @Query("SELECT c FROM Challenge c WHERE " +
            "((c.challenger.username = :user1 AND c.challenged.username = :user2) OR " +
            "(c.challenger.username = :user2 AND c.challenged.username = :user1)) AND " +
            "c.status = 'PENDING'")
    Optional<Challenge> findPendingChallengeBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);
}