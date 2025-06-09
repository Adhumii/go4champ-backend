package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.model.TrainingsPlan;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.TrainingsPlanRepo;
import com.go4champ.go4champ.repo.UserRepo;
import com.go4champ.go4champ.repo.TrainingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingsPlanService {

    private final TrainingsPlanRepo trainingsPlanRepository;
    private final UserRepo userRepository;
    private final TrainingRepo trainingRepository;

    @Autowired
    public TrainingsPlanService(TrainingsPlanRepo trainingsPlanRepository,
                                UserRepo userRepository,
                                TrainingRepo trainingRepository) {
        this.trainingsPlanRepository = trainingsPlanRepository;
        this.userRepository = userRepository;
        this.trainingRepository = trainingRepository;
    }

    // Alle Trainingspläne abrufen
    public List<TrainingsPlan> getAllTrainingPlans() {
        return trainingsPlanRepository.findAll();
    }

    // Trainingsplan anhand der ID abrufen
    public Optional<TrainingsPlan> getTrainingsPlanById(int planId) {
        return trainingsPlanRepository.findById(planId);
    }

    // Alle Trainingspläne eines Users abrufen
    public List<TrainingsPlan> getTrainingsPlansByUser(User user) {
        return trainingsPlanRepository.findByUser(user);
    }

    // Alle Trainingspläne eines Users anhand des Usernames abrufen
    public List<TrainingsPlan> getTrainingsPlansByUsername(String username) {
        return trainingsPlanRepository.findByUserUsername(username);
    }

    // Trainingsplan erstellen mit Username
    @Transactional
    public TrainingsPlan createTrainingsPlan(TrainingsPlan trainingsPlan, String username) {
        Optional<User> userOptional = userRepository.findById(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            trainingsPlan.setUser(user);
            return trainingsPlanRepository.save(trainingsPlan);
        } else {
            throw new RuntimeException("Benutzer mit Username " + username + " nicht gefunden!");
        }
    }

    // Trainingsplan erstellen ohne direkte Benutzerzuweisung
    @Transactional
    public TrainingsPlan createTrainingsPlan(TrainingsPlan trainingsPlan) {
        return trainingsPlanRepository.save(trainingsPlan);
    }

    // Trainingsplan aktualisieren
    @Transactional
    public TrainingsPlan updateTrainingsPlan(int planId, TrainingsPlan updatedPlan) {
        Optional<TrainingsPlan> existingPlan = trainingsPlanRepository.findById(planId);

        if (existingPlan.isPresent()) {
            TrainingsPlan plan = existingPlan.get();
            plan.setPlanName(updatedPlan.getPlanName());
            plan.setDescription(updatedPlan.getDescription());

            // User-Zuordnung nicht ändern, um Beziehung zu erhalten
            if (updatedPlan.getTrainings() != null) {
                plan.setTrainings(updatedPlan.getTrainings());
            }

            return trainingsPlanRepository.save(plan);
        }

        throw new RuntimeException("Trainingsplan mit ID " + planId + " nicht gefunden!");
    }

    // Trainingsplan löschen
    @Transactional
    public void deleteTrainingsPlan(int planId) {
        if (trainingsPlanRepository.existsById(planId)) {
            trainingsPlanRepository.deleteById(planId);
        } else {
            throw new RuntimeException("Trainingsplan mit ID " + planId + " nicht gefunden!");
        }
    }

    // Training zu einem Plan hinzufügen (über Training ID)
    @Transactional
    public TrainingsPlan addTrainingToPlan(int planId, int trainingId) {
        Optional<TrainingsPlan> planOptional = trainingsPlanRepository.findById(planId);
        Optional<Training> trainingOptional = trainingRepository.findById(trainingId);

        if (planOptional.isPresent() && trainingOptional.isPresent()) {
            TrainingsPlan plan = planOptional.get();
            Training training = trainingOptional.get();
            plan.addTraining(training);
            return trainingsPlanRepository.save(plan);
        }

        throw new RuntimeException("Trainingsplan oder Training nicht gefunden!");
    }

    // Training aus einem Plan entfernen
    @Transactional
    public TrainingsPlan removeTrainingFromPlan(int planId, int trainingId) {
        Optional<TrainingsPlan> planOptional = trainingsPlanRepository.findById(planId);

        if (planOptional.isPresent()) {
            TrainingsPlan plan = planOptional.get();
            plan.removeTrainingById(trainingId);
            return trainingsPlanRepository.save(plan);
        }

        throw new RuntimeException("Trainingsplan mit ID " + planId + " nicht gefunden!");
    }

    // Trainingsplan einem User zuweisen
    @Transactional
    public TrainingsPlan assignPlanToUser(int planId, String username) {
        Optional<TrainingsPlan> planOptional = trainingsPlanRepository.findById(planId);
        Optional<User> userOptional = userRepository.findById(username);

        if (planOptional.isPresent() && userOptional.isPresent()) {
            TrainingsPlan plan = planOptional.get();
            User user = userOptional.get();

            plan.setUser(user);
            return trainingsPlanRepository.save(plan);
        } else {
            throw new RuntimeException("Trainingsplan oder Benutzer nicht gefunden!");
        }
    }

    // Suche nach Trainingsplänen anhand des Namens
    public List<TrainingsPlan> searchTrainingsPlansByName(String planName) {
        return trainingsPlanRepository.findByPlanNameContainingIgnoreCase(planName);
    }

    // Prüfen ob ein Planname bereits existiert
    public boolean planNameExists(String planName) {
        return trainingsPlanRepository.existsByPlanName(planName);
    }

    // Prüfen ob ein User bereits einen Plan mit diesem Namen hat
    public boolean userHasPlanWithName(String planName, User user) {
        return trainingsPlanRepository.existsByPlanNameAndUser(planName, user);
    }

    // Trainingspläne mit mindestens X Trainings finden
    public List<TrainingsPlan> getTrainingsPlansByMinTrainingCount(int minCount) {
        return trainingsPlanRepository.findByMinTrainingCount(minCount);
    }
}