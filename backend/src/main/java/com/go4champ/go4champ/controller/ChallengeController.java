package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.dto.*;
import com.go4champ.go4champ.service.ChallengeService;
import com.go4champ.go4champ.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/challenges")
@CrossOrigin(origins = "*")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @PostMapping
    public ResponseEntity<?> createChallenge(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateChallengeRequest request) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            ChallengeResponse response = challengeService.createChallenge(username, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Challenge akzeptieren
     */
    @PostMapping("/{challengeId}/accept")
    public ResponseEntity<?> acceptChallenge(
            @RequestHeader("Authorization") String token,
            @PathVariable Long challengeId) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            ChallengeResponse response = challengeService.acceptChallenge(username, challengeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Challenge ablehnen
     */
    @PostMapping("/{challengeId}/reject")
    public ResponseEntity<?> rejectChallenge(
            @RequestHeader("Authorization") String token,
            @PathVariable Long challengeId) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            challengeService.rejectChallenge(username, challengeId);
            return ResponseEntity.ok(Map.of("message", "Challenge abgelehnt"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Challenge abbrechen
     */
    @PostMapping("/{challengeId}/cancel")
    public ResponseEntity<?> cancelChallenge(
            @RequestHeader("Authorization") String token,
            @PathVariable Long challengeId) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            challengeService.cancelChallenge(username, challengeId);
            return ResponseEntity.ok(Map.of("message", "Challenge abgebrochen"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Challenge Ergebnis einreichen
     */
    @PostMapping("/{challengeId}/submit-result")
    public ResponseEntity<?> submitResult(
            @RequestHeader("Authorization") String token,
            @PathVariable Long challengeId,
            @Valid @RequestBody SubmitChallengeResultRequest request) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            ChallengeResponse response = challengeService.submitResult(username, challengeId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Sieger bestimmen (nur FREE Type)
     */
    @PostMapping("/{challengeId}/declare-winner")
    public ResponseEntity<?> declareWinner(
            @RequestHeader("Authorization") String token,
            @PathVariable Long challengeId,
            @Valid @RequestBody DeclareWinnerRequest request) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            ChallengeResponse response = challengeService.declareWinner(username, challengeId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Alle Challenges eines Users
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyChallenges(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            List<ChallengeResponse> challenges = challengeService.getAllChallenges(username);
            return ResponseEntity.ok(Map.of(
                    "challenges", challenges,
                    "count", challenges.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Eingehende Challenges
     */
    @GetMapping("/incoming")
    public ResponseEntity<?> getIncomingChallenges(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            List<ChallengeResponse> challenges = challengeService.getIncomingChallenges(username);
            return ResponseEntity.ok(Map.of(
                    "challenges", challenges,
                    "count", challenges.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ausgehende Challenges
     */
    @GetMapping("/outgoing")
    public ResponseEntity<?> getOutgoingChallenges(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            List<ChallengeResponse> challenges = challengeService.getOutgoingChallenges(username);
            return ResponseEntity.ok(Map.of(
                    "challenges", challenges,
                    "count", challenges.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Aktive Challenges
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveChallenges(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            List<ChallengeResponse> challenges = challengeService.getActiveChallenges(username);
            return ResponseEntity.ok(Map.of(
                    "challenges", challenges,
                    "count", challenges.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Challenge Details
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<?> getChallengeDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable Long challengeId) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            ChallengeResponse challenge = challengeService.getChallenge(username, challengeId);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Challenge Übersicht/Statistiken
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getChallengeOverview(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            ChallengeOverviewResponse overview = challengeService.getChallengeOverview(username);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}