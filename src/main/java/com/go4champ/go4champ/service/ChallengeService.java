package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.*;
import com.go4champ.go4champ.repo.ChallengeRepository;
import com.go4champ.go4champ.repo.UserRepo;
import com.go4champ.go4champ.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private FriendshipService friendshipService;

    /**
     * Erstellt eine neue Challenge
     */
    @Transactional
    public ChallengeResponse createChallenge(String challengerUsername, CreateChallengeRequest request) {
        // Validation
        User challenger = userRepository.findByUsername(challengerUsername)
                .orElseThrow(() -> new RuntimeException("Challenger nicht gefunden"));

        User challenged = userRepository.findByUsername(request.getChallengedUsername())
                .orElseThrow(() -> new RuntimeException("Herausgeforderter User nicht gefunden"));

        // Prüfe ob sie Freunde sind
        if (!friendshipService.areFriends(challengerUsername, request.getChallengedUsername())) {
            throw new RuntimeException("Ihr müsst Freunde sein, um Challenges zu erstellen");
        }

        // Prüfe ob bereits eine pending Challenge zwischen ihnen existiert
        Optional<Challenge> existingChallenge = challengeRepository
                .findPendingChallengeBetweenUsers(challengerUsername, request.getChallengedUsername());
        if (existingChallenge.isPresent()) {
            throw new RuntimeException("Es existiert bereits eine ausstehende Challenge zwischen euch");
        }

        // Validierung für REPS und WEIGHT Types
        if (request.getType() == ChallengeType.REPS || request.getType() == ChallengeType.WEIGHT) {
            if (request.getTargetValue() == null || request.getTargetValue() <= 0) {
                throw new RuntimeException("Target Value ist für " + request.getType() + " Challenges erforderlich");
            }
            if (request.getTargetUnit() == null || request.getTargetUnit().trim().isEmpty()) {
                throw new RuntimeException("Target Unit ist für " + request.getType() + " Challenges erforderlich");
            }
        }

        // Challenge erstellen
        Challenge challenge = new Challenge();
        challenge.setChallenger(challenger);
        challenge.setChallenged(challenged);
        challenge.setType(request.getType());
        challenge.setStatus(ChallengeStatus.PENDING);
        challenge.setTitle(request.getTitle());
        challenge.setDescription(request.getDescription());
        challenge.setTargetValue(request.getTargetValue());
        challenge.setTargetUnit(request.getTargetUnit());
        challenge.setDeadline(request.getDeadline());

        Challenge savedChallenge = challengeRepository.save(challenge);

        return toChallengeResponse(savedChallenge, challengerUsername);
    }

    /**
     * Challenge akzeptieren
     */
    @Transactional
    public ChallengeResponse acceptChallenge(String username, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge nicht gefunden"));

        // Validierung
        if (!challenge.getChallenged().getUsername().equals(username)) {
            throw new RuntimeException("Nur der Herausgeforderte kann die Challenge akzeptieren");
        }

        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new RuntimeException("Challenge kann nicht mehr akzeptiert werden");
        }

        // Challenge akzeptieren
        challenge.setStatus(ChallengeStatus.ACCEPTED);
        challenge.setAcceptedAt(LocalDateTime.now());

        Challenge savedChallenge = challengeRepository.save(challenge);

        return toChallengeResponse(savedChallenge, username);
    }

    /**
     * Challenge ablehnen
     */
    @Transactional
    public void rejectChallenge(String username, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge nicht gefunden"));

        // Validierung
        if (!challenge.getChallenged().getUsername().equals(username)) {
            throw new RuntimeException("Nur der Herausgeforderte kann die Challenge ablehnen");
        }

        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new RuntimeException("Challenge kann nicht mehr abgelehnt werden");
        }

        // Challenge löschen (oder Status auf REJECTED setzen)
        challengeRepository.delete(challenge);
    }

    /**
     * Challenge Ergebnis einreichen
     */
    @Transactional
    public ChallengeResponse submitResult(String username, Long challengeId, SubmitChallengeResultRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge nicht gefunden"));

        // Validierung
        if (!challenge.isParticipant(username)) {
            throw new RuntimeException("Sie sind nicht an dieser Challenge beteiligt");
        }

        if (challenge.getStatus() != ChallengeStatus.ACCEPTED) {
            throw new RuntimeException("Challenge ist nicht aktiv");
        }

        // Ergebnis setzen
        if (challenge.getChallenger().getUsername().equals(username)) {
            if (challenge.isChallengerSubmitted()) {
                throw new RuntimeException("Sie haben bereits ein Ergebnis eingereicht");
            }
            challenge.setChallengerResult(request.getResult());
            challenge.setChallengerSubmitted(true);
        } else {
            if (challenge.isChallengedSubmitted()) {
                throw new RuntimeException("Sie haben bereits ein Ergebnis eingereicht");
            }
            challenge.setChallengedResult(request.getResult());
            challenge.setChallengedSubmitted(true);
        }

        // KORREKTUR: Prüfe ob Challenge abgeschlossen werden kann
        if (challenge.getType() != ChallengeType.FREE && challenge.isCompleted()) {
            // WICHTIG: Winner bestimmen für REPS und WEIGHT Challenges
            User winner = challenge.determineWinner();
            challenge.setWinner(winner);

            challenge.setStatus(ChallengeStatus.COMPLETED);
            challenge.setCompletedAt(LocalDateTime.now());
        }

        Challenge savedChallenge = challengeRepository.save(challenge);

        return toChallengeResponse(savedChallenge, username);
    }

    /**
     * Sieger bestimmen (nur für FREE Type)
     */
    @Transactional
    public ChallengeResponse declareWinner(String username, Long challengeId, DeclareWinnerRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge nicht gefunden"));

        // Validierung
        if (!challenge.getChallenger().getUsername().equals(username)) {
            throw new RuntimeException("Nur der Herausforderer kann den Sieger bestimmen");
        }

        if (challenge.getType() != ChallengeType.FREE) {
            throw new RuntimeException("Sieger können nur bei FREE Type Challenges bestimmt werden");
        }

        if (!challenge.isChallengerSubmitted() || !challenge.isChallengedSubmitted()) {
            throw new RuntimeException("Beide Teilnehmer müssen erst ihre Ergebnisse einreichen");
        }

        if (challenge.getWinner() != null) {
            throw new RuntimeException("Sieger wurde bereits bestimmt");
        }

        // Sieger setzen
        User winner = null;
        if ("challenger".equals(request.getWinnerUsername())) {
            winner = challenge.getChallenger();
        } else if ("challenged".equals(request.getWinnerUsername())) {
            winner = challenge.getChallenged();
        } else if (!"tie".equals(request.getWinnerUsername())) {
            throw new RuntimeException("Ungültiger Winner Username");
        }

        challenge.setWinner(winner);
        challenge.setStatus(ChallengeStatus.COMPLETED);
        challenge.setCompletedAt(LocalDateTime.now());

        Challenge savedChallenge = challengeRepository.save(challenge);

        return toChallengeResponse(savedChallenge, username);
    }

    /**
     * Holt alle Challenges eines Users
     */
    public List<ChallengeResponse> getAllChallenges(String username) {
        List<Challenge> challenges = challengeRepository.findAllByUsername(username);
        return challenges.stream()
                .map(challenge -> toChallengeResponse(challenge, username))
                .collect(Collectors.toList());
    }

    /**
     * Holt eingehende Challenges (PENDING)
     */
    public List<ChallengeResponse> getIncomingChallenges(String username) {
        List<Challenge> challenges = challengeRepository.findPendingChallenges(username);
        return challenges.stream()
                .map(challenge -> toChallengeResponse(challenge, username))
                .collect(Collectors.toList());
    }

    /**
     * Holt ausgehende Challenges
     */
    public List<ChallengeResponse> getOutgoingChallenges(String username) {
        List<Challenge> challenges = challengeRepository.findOutgoingChallenges(username);
        return challenges.stream()
                .map(challenge -> toChallengeResponse(challenge, username))
                .collect(Collectors.toList());
    }

    /**
     * Holt aktive Challenges
     */
    public List<ChallengeResponse> getActiveChallenges(String username) {
        List<Challenge> challenges = challengeRepository.findActiveChallenges(username);
        return challenges.stream()
                .map(challenge -> toChallengeResponse(challenge, username))
                .collect(Collectors.toList());
    }

    /**
     * Holt Challenge Übersicht/Statistiken - KORRIGIERT
     */
    public ChallengeOverviewResponse getChallengeOverview(String username) {
        List<Challenge> completedChallenges = challengeRepository.findCompletedChallenges(username);

        long totalChallenges = challengeRepository.findAllByUsername(username).size();
        long activeChallenges = challengeRepository.findActiveChallenges(username).size();
        long pendingIncoming = challengeRepository.findPendingChallenges(username).size();
        long pendingOutgoing = challengeRepository.findOutgoingChallenges(username).stream()
                .filter(c -> c.getStatus() == ChallengeStatus.PENDING)
                .count();

        // KORRIGIERTE Statistik-Berechnung
        long wonChallenges = 0;
        long lostChallenges = 0;
        long repsWon = 0, weightWon = 0, freeWon = 0;
        long repsLost = 0, weightLost = 0, freeLost = 0;

        for (Challenge challenge : completedChallenges) {
            boolean userWon = false;

            if (challenge.getType() == ChallengeType.FREE) {
                // Bei FREE Type: Winner direkt prüfen
                userWon = challenge.getWinner() != null &&
                        challenge.getWinner().getUsername().equals(username);
            } else {
                // Bei REPS/WEIGHT: Ergebnisse vergleichen
                if (challenge.getChallengerResult() != null && challenge.getChallengedResult() != null) {
                    if (challenge.getChallenger().getUsername().equals(username)) {
                        userWon = challenge.getChallengerResult() > challenge.getChallengedResult();
                    } else if (challenge.getChallenged().getUsername().equals(username)) {
                        userWon = challenge.getChallengedResult() > challenge.getChallengerResult();
                    }
                }
            }

            if (userWon) {
                wonChallenges++;
                switch (challenge.getType()) {
                    case REPS -> repsWon++;
                    case WEIGHT -> weightWon++;
                    case FREE -> freeWon++;
                }
            } else {
                // Nur als verloren zählen wenn es keinen Tie gibt
                boolean isTie = false;
                if (challenge.getType() == ChallengeType.FREE) {
                    isTie = challenge.getWinner() == null;
                } else {
                    isTie = challenge.getChallengerResult() != null &&
                            challenge.getChallengedResult() != null &&
                            challenge.getChallengerResult().equals(challenge.getChallengedResult());
                }

                if (!isTie) {
                    lostChallenges++;
                    switch (challenge.getType()) {
                        case REPS -> repsLost++;
                        case WEIGHT -> weightLost++;
                        case FREE -> freeLost++;
                    }
                }
            }
        }

        double winRate = (totalChallenges > 0) ? (double) wonChallenges / totalChallenges * 100 : 0.0;

        ChallengeOverviewResponse overview = new ChallengeOverviewResponse();
        overview.setTotalChallenges(totalChallenges);
        overview.setWonChallenges(wonChallenges);
        overview.setLostChallenges(lostChallenges);
        overview.setActiveChallenges(activeChallenges);
        overview.setPendingIncoming(pendingIncoming);
        overview.setPendingOutgoing(pendingOutgoing);
        overview.setWinRate(Math.round(winRate * 100.0) / 100.0);
        overview.setRepsWon(repsWon);
        overview.setWeightWon(weightWon);
        overview.setFreeWon(freeWon);
        overview.setRepsLost(repsLost);
        overview.setWeightLost(weightLost);
        overview.setFreeLost(freeLost);

        return overview;
    }

    /**
     * Konvertiert Challenge Entity zu ChallengeResponse DTO
     */
    private ChallengeResponse toChallengeResponse(Challenge challenge, String currentUsername) {
        ChallengeResponse response = new ChallengeResponse();

        response.setId(challenge.getId());
        response.setChallengerUsername(challenge.getChallenger().getUsername());
        response.setChallengerName(challenge.getChallenger().getName());
        response.setChallengedUsername(challenge.getChallenged().getUsername());
        response.setChallengedName(challenge.getChallenged().getName());
        response.setType(challenge.getType());
        response.setStatus(challenge.getStatus());
        response.setTitle(challenge.getTitle());
        response.setDescription(challenge.getDescription());
        response.setTargetValue(challenge.getTargetValue());
        response.setTargetUnit(challenge.getTargetUnit());
        response.setChallengerResult(challenge.getChallengerResult());
        response.setChallengedResult(challenge.getChallengedResult());
        response.setChallengerSubmitted(challenge.isChallengerSubmitted());
        response.setChallengedSubmitted(challenge.isChallengedSubmitted());
        response.setCreatedAt(challenge.getCreatedAt());
        response.setAcceptedAt(challenge.getAcceptedAt());
        response.setCompletedAt(challenge.getCompletedAt());
        response.setDeadline(challenge.getDeadline());

        // Winner
        if (challenge.getWinner() != null) {
            response.setWinnerUsername(challenge.getWinner().getUsername());
            response.setWinnerName(challenge.getWinner().getName());
        }

        // Expired?
        response.setExpired(challenge.getDeadline() != null &&
                challenge.getDeadline().isBefore(LocalDateTime.now()) &&
                challenge.getStatus() != ChallengeStatus.COMPLETED);

        // My Role
        if (challenge.getChallenger().getUsername().equals(currentUsername)) {
            response.setMyRole("CHALLENGER");
        } else if (challenge.getChallenged().getUsername().equals(currentUsername)) {
            response.setMyRole("CHALLENGED");
        }

        return response;
    }

    /**
     * Holt eine spezifische Challenge
     */
    public ChallengeResponse getChallenge(String username, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge nicht gefunden"));

        // Prüfe ob User an der Challenge beteiligt ist
        if (!challenge.isParticipant(username)) {
            throw new RuntimeException("Sie sind nicht an dieser Challenge beteiligt");
        }

        return toChallengeResponse(challenge, username);
    }

    /**
     * Challenge abbrechen (nur vom Ersteller)
     */
    @Transactional
    public void cancelChallenge(String username, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge nicht gefunden"));

        // Nur der Herausforderer kann abbrechen
        if (!challenge.getChallenger().getUsername().equals(username)) {
            throw new RuntimeException("Nur der Herausforderer kann die Challenge abbrechen");
        }

        // Nur pending oder accepted Challenges können abgebrochen werden
        if (challenge.getStatus() != ChallengeStatus.PENDING &&
                challenge.getStatus() != ChallengeStatus.ACCEPTED) {
            throw new RuntimeException("Challenge kann nicht mehr abgebrochen werden");
        }

        challenge.setStatus(ChallengeStatus.CANCELLED);
        challengeRepository.save(challenge);
    }
}