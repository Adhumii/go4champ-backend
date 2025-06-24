package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    @Autowired
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    // Alle Trainings abrufen
    @GetMapping
    @Operation(summary = "Alle Trainingseinheiten abrufen")
    public ResponseEntity<List<Training>> getAllTrainings() {
        List<Training> trainings = trainingService.getAllTrainings();
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }

    // Training nach ID abrufen
    @GetMapping("/{id}")
    @Operation(summary = "Training nach ID abrufen")

    public ResponseEntity<Training> getTrainingById(@PathVariable int id) {
        Optional<Training> training = trainingService.getTrainingById(id);
        return training.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Trainings eines bestimmten Benutzers abrufen
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')") // Nur für Admins!
    @Operation(summary = "Trainingseinheiten eines Benutzers abrufen")

    public ResponseEntity<List<Training>> getTrainingsByUsername(@PathVariable String username) {
        List<Training> trainings = trainingService.getTrainingsByUsername(username);
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }

    // Neues Training erstellen ohne direkte Benutzerzuweisung
    @PostMapping
    @Operation(summary = "Neue Trainingseinheit erstellen")
    public ResponseEntity<Training> createTraining(@RequestBody Training training) {
        Training newTraining = trainingService.createTraining(training);
        return new ResponseEntity<>(newTraining, HttpStatus.CREATED);
    }

    // Neues Training erstellen mit direkter Benutzerzuweisung
    @PostMapping("/user/{username}")
    @Operation(summary = "Neue Trainingseinheit für bestehenden Benutzer erstellen")

    public ResponseEntity<Training> createTrainingForUser(@RequestBody Training training, @PathVariable String username) {
        try {
            Training newTraining = trainingService.createTraining(training, username);
            return new ResponseEntity<>(newTraining, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Training einem Benutzer zuweisen
    @PutMapping("/{id}/assign/{username}")
    @Operation(summary = "Trainingseinheit einem Benutzer zuweisen")

    public ResponseEntity<Training> assignTrainingToUser(@PathVariable int id, @PathVariable String username) {
        try {
            Training updatedTraining = trainingService.assignTrainingToUser(id, username);
            return new ResponseEntity<>(updatedTraining, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Training aktualisieren
    @PutMapping("/{id}")
    @Operation(summary = "Trainingseinheit aktualisieren")

    public ResponseEntity<Training> updateTraining(@PathVariable int id, @RequestBody Training training) {
        try {
            Training updatedTraining = trainingService.updateTraining(id, training);
            return new ResponseEntity<>(updatedTraining, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Training löschen
    @DeleteMapping("/{id}")
    @Operation(summary = "Trainingseinheit löschen")

    public ResponseEntity<HttpStatus> deleteTraining(@PathVariable int id) {
        try {
            trainingService.deleteTraining(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}