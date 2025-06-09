package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.TrainingsPlan;
import com.go4champ.go4champ.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingsPlanRepo extends JpaRepository<TrainingsPlan, Integer> {

    // Finde alle Trainingspläne eines bestimmten Users
    List<TrainingsPlan> findByUser(User user);

    // Finde alle Trainingspläne eines Users anhand des Usernames
    List<TrainingsPlan> findByUserUsername(String username);

    // Finde Trainingsplan anhand des Namens
    Optional<TrainingsPlan> findByPlanName(String planName);

    // Finde Trainingspläne die einen bestimmten Text im Namen enthalten
    List<TrainingsPlan> findByPlanNameContainingIgnoreCase(String planName);

    // Finde Trainingspläne mit einer bestimmten Anzahl von Trainings
    @Query("SELECT tp FROM TrainingsPlan tp WHERE SIZE(tp.trainings) = :trainingCount")
    List<TrainingsPlan> findByTrainingCount(@Param("trainingCount") int trainingCount);

    // Finde Trainingspläne mit mindestens einer bestimmten Anzahl von Trainings
    @Query("SELECT tp FROM TrainingsPlan tp WHERE SIZE(tp.trainings) >= :minTrainingCount")
    List<TrainingsPlan> findByMinTrainingCount(@Param("minTrainingCount") int minTrainingCount);

    // Prüfe ob ein Trainingsplan mit diesem Namen bereits existiert
    boolean existsByPlanName(String planName);

    // Prüfe ob ein User bereits einen Plan mit diesem Namen hat
    boolean existsByPlanNameAndUser(String planName, User user);
}