package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.TrainingRepo;
import com.go4champ.go4champ.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private final TrainingRepo trainingRepository;
    private final UserRepo userRepository;

    @Autowired
    public TrainingService(TrainingRepo trainingRepository, UserRepo userRepository) {
        this.trainingRepository = trainingRepository;
        this.userRepository = userRepository;
    }

    // Alle Trainings abrufen
    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    // Training nach ID abrufen
    public Optional<Training> getTrainingById(int id) {
        return trainingRepository.findById(id);
    }

    // Trainings eines bestimmten Benutzers abrufen
    public List<Training> getTrainingsByUser(User user) {
        return trainingRepository.findByUser(user);
    }

    // Trainings eines Benutzers über Username abrufen
    public List<Training> getTrainingsByUsername(String username) {
        return trainingRepository.findByUserUsername(username);
    }

    // Neues Training erstellen mit direkter Benutzerzuweisung
    @Transactional
    public Training createTraining(Training training, String username) {
        Optional<User> userOptional = userRepository.findById(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            training.setUser(user);
            return trainingRepository.save(training);
        } else {
            throw new RuntimeException("Benutzer mit Username " + username + " nicht gefunden!");
        }
    }

    // Neues Training erstellen ohne direkte Benutzerzuweisung
    @Transactional
    public Training createTraining(Training training) {
        return trainingRepository.save(training);
    }

    // Training einem Benutzer zuweisen
    @Transactional
    public Training assignTrainingToUser(int trainingId, String username) {
        Optional<Training> trainingOptional = trainingRepository.findById(trainingId);
        Optional<User> userOptional = userRepository.findById(username);

        if (trainingOptional.isPresent() && userOptional.isPresent()) {
            Training training = trainingOptional.get();
            User user = userOptional.get();

            training.setUser(user);
            return trainingRepository.save(training);
        } else {
            throw new RuntimeException("Training oder Benutzer nicht gefunden!");
        }
    }

    // Training aktualisieren
    @Transactional
    public Training updateTraining(int id, Training updatedTraining) {
        Optional<Training> existingTrainingOptional = trainingRepository.findById(id);

        if (existingTrainingOptional.isPresent()) {
            Training existingTraining = existingTrainingOptional.get();

            // Aktualisiere die Felder
            existingTraining.setTitle(updatedTraining.getTitle());
            existingTraining.setDescription(updatedTraining.getDescription());
            existingTraining.setDifficulty(updatedTraining.getDifficulty());
            existingTraining.setType(updatedTraining.isType());
            existingTraining.setDuration(updatedTraining.getDuration());

            // User-Zuordnung nicht ändern, um Beziehung zu erhalten

            return trainingRepository.save(existingTraining);
        } else {
            throw new RuntimeException("Training mit ID " + id + " nicht gefunden!");
        }
    }

    // Training löschen
    @Transactional
    public void deleteTraining(int id) {
        if (trainingRepository.existsById(id)) {
            trainingRepository.deleteById(id);
        } else {
            throw new RuntimeException("Training mit ID " + id + " nicht gefunden!");
        }
    }
}