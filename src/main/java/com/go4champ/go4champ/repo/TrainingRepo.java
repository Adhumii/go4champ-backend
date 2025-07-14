package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepo extends JpaRepository<Training, Integer> {

    // =============================================================================
    // BESTEHENDE METHODEN
    // =============================================================================

    List<Training> findByUser(User user);
    List<Training> findByUserUsername(String username);

    /**
     * Zählt alle Trainings eines Users
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username")
    Long countByUserUsername(@Param("username") String username);

    // =============================================================================
    // DATUMS-BASIERTE QUERIES FÜR RANKING SYSTEM
    // =============================================================================

    /**
     * Prüft ob ein User heute bereits trainiert hat
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Training t WHERE t.user.username = :username AND FUNCTION('DATE', t.createdAt) = CURRENT_DATE")
    boolean hasTrainedToday(@Param("username") String username);

    /**
     * Findet Trainings eines Users nach einem bestimmten Datum
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username AND t.createdAt >= :fromDate ORDER BY t.createdAt DESC")
    List<Training> findByUsernameAfterDate(@Param("username") String username, @Param("fromDate") LocalDateTime fromDate);

    /**
     * Findet Trainings eines Users zwischen zwei Daten
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Training> findByUsernameBetweenDates(@Param("username") String username,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Zählt Trainings eines Users nach einem bestimmten Datum
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username AND t.createdAt >= :fromDate")
    Long countByUsernameAfterDate(@Param("username") String username, @Param("fromDate") LocalDateTime fromDate);

    /**
     * Zählt Trainings eines Users zwischen zwei Daten
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countByUsernameBetweenDates(@Param("username") String username,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Findet das neueste Training eines Users
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username ORDER BY t.createdAt DESC LIMIT 1")
    Optional<Training> findLatestByUsername(@Param("username") String username);

    /**
     * Findet alle Trainings eines Users für einen bestimmten Monat
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username " +
            "AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month " +
            "ORDER BY t.createdAt DESC")
    List<Training> findByUsernameForMonth(@Param("username") String username,
                                          @Param("year") int year,
                                          @Param("month") int month);

    /**
     * Zählt Trainings eines Users für einen bestimmten Monat
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username " +
            "AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    Long countByUsernameForMonth(@Param("username") String username,
                                 @Param("year") int year,
                                 @Param("month") int month);

    /**
     * Findet alle Trainings eines Users für eine bestimmte Woche
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username " +
            "AND YEAR(t.createdAt) = :year AND WEEK(t.createdAt) = :week " +
            "ORDER BY t.createdAt DESC")
    List<Training> findByUsernameForWeek(@Param("username") String username,
                                         @Param("year") int year,
                                         @Param("week") int week);

    /**
     * Zählt Trainings eines Users für eine bestimmte Woche
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username " +
            "AND YEAR(t.createdAt) = :year AND WEEK(t.createdAt) = :week")
    Long countByUsernameForWeek(@Param("username") String username,
                                @Param("year") int year,
                                @Param("week") int week);

    /**
     * Findet alle Trainings eines Users für ein bestimmtes Datum
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username AND FUNCTION('DATE', t.createdAt) = :date ORDER BY t.createdAt DESC")
    List<Training> findByUsernameForDate(@Param("username") String username, @Param("date") LocalDate date);

    /**
     * Zählt Trainings eines Users für ein bestimmtes Datum
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username AND FUNCTION('DATE', t.createdAt) = :date")
    Long countByUsernameForDate(@Param("username") String username, @Param("date") LocalDate date);

    /**
     * FIXED: Findet alle Trainings eines Users sortiert nach Datum (für Streak-Berechnung)
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username ORDER BY t.createdAt DESC")
    List<Training> findByUsernameOrderByDateDesc(@Param("username") String username);

    /**
     * Findet alle Trainings sortiert nach Datum aufsteigend
     */
    @Query("SELECT t FROM Training t WHERE t.user.username = :username ORDER BY t.createdAt ASC")
    List<Training> findByUsernameOrderByDateAsc(@Param("username") String username);

    // =============================================================================
    // GLOBALE RANKING-QUERIES
    // =============================================================================

    /**
     * Findet User mit den meisten Trainings (für globales Ranking)
     */
    @Query("SELECT t.user.username, COUNT(t) FROM Training t GROUP BY t.user.username ORDER BY COUNT(t) DESC")
    List<Object[]> findUsersWithMostTrainings();

    /**
     * Findet User mit den meisten Trainings diesen Monat
     */
    @Query("SELECT t.user.username, COUNT(t) FROM Training t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month " +
            "GROUP BY t.user.username ORDER BY COUNT(t) DESC")
    List<Object[]> findUsersWithMostTrainingsThisMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Findet User mit den meisten Trainings diese Woche
     */
    @Query("SELECT t.user.username, COUNT(t) FROM Training t " +
            "WHERE YEAR(t.createdAt) = :year AND WEEK(t.createdAt) = :week " +
            "GROUP BY t.user.username ORDER BY COUNT(t) DESC")
    List<Object[]> findUsersWithMostTrainingsThisWeek(@Param("year") int year, @Param("week") int week);

    /**
     * Findet User mit den meisten Trainings in einem bestimmten Zeitraum
     */
    @Query("SELECT t.user.username, COUNT(t) FROM Training t " +
            "WHERE t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY t.user.username ORDER BY COUNT(t) DESC")
    List<Object[]> findUsersWithMostTrainingsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    // =============================================================================
    // STATISTIK-QUERIES
    // =============================================================================

    /**
     * Berechnet die durchschnittliche Trainings-Dauer eines Users
     */
    @Query("SELECT AVG(t.duration) FROM Training t WHERE t.user.username = :username")
    Double findAverageTrainingDurationByUsername(@Param("username") String username);

    /**
     * Berechnet die durchschnittliche Schwierigkeit der Trainings eines Users
     */
    @Query("SELECT AVG(t.difficulty) FROM Training t WHERE t.user.username = :username")
    Double findAverageTrainingDifficultyByUsername(@Param("username") String username);

    /**
     * Berechnet die Gesamt-Trainingszeit eines Users
     */
    @Query("SELECT SUM(t.duration) FROM Training t WHERE t.user.username = :username")
    Long findTotalTrainingTimeByUsername(@Param("username") String username);

    /**
     * Zählt Indoor vs Outdoor Trainings
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username AND t.type = :isIndoor")
    Long countByUsernameAndType(@Param("username") String username, @Param("isIndoor") boolean isIndoor);

    /**
     * Findet die höchste Schwierigkeit, die ein User gemeistert hat
     */
    @Query("SELECT MAX(t.difficulty) FROM Training t WHERE t.user.username = :username")
    Double findMaxDifficultyByUsername(@Param("username") String username);

    /**
     * Zählt abgeschlossene Trainings eines Users
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.user.username = :username AND t.completedAt IS NOT NULL")
    Long countCompletedTrainingsByUsername(@Param("username") String username);

    /**
     * Findet die letzten N Trainings eines Users
     */
    List<Training> findTop10ByUserUsernameOrderByCreatedAtDesc(String username);

    // REMOVED: Die problematische findDistinctTrainingDatesByUsername Methode wurde entfernt
    // REPLACED BY: findByUsernameOrderByDateDesc() für bessere Streak-Berechnung
}