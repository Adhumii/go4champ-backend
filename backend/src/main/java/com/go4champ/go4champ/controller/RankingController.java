package com.go4champ.go4champ.controller;

import com.go4champ.go4champ.dto.*;
import com.go4champ.go4champ.service.RankingService;
import com.go4champ.go4champ.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rankings")
@CrossOrigin(origins = "*")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Holt die komplette Ranking Übersicht für einen User
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getRankingOverview(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            RankingOverviewResponse overview = rankingService.getRankingOverview(username);
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Holt User-Statistiken
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            UserStats stats = rankingService.calculateUserStats(username);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Globales Training-Ranking (Gesamte Trainings)
     */
    @GetMapping("/global/trainings")
    public ResponseEntity<?> getGlobalTrainingRanking(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            RankingResponse ranking = rankingService.getGlobalTrainingRanking(username, limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Monatliches Training-Ranking
     */
    @GetMapping("/global/monthly")
    public ResponseEntity<?> getMonthlyTrainingRanking(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            RankingResponse ranking = rankingService.getMonthlyTrainingRanking(username, limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Streak-Ranking
     */
    @GetMapping("/global/streaks")
    public ResponseEntity<?> getStreakRanking(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            RankingResponse ranking = rankingService.getStreakRanking(username, limit);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Friend-Ranking für monatliche Trainings
     */
    @GetMapping("/friends/monthly")
    public ResponseEntity<?> getFriendMonthlyRanking(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            FriendRankingResponse ranking = rankingService.getFriendMonthlyRanking(username);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Friend-Ranking für Streaks
     */
    @GetMapping("/friends/streaks")
    public ResponseEntity<?> getFriendStreakRanking(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            FriendRankingResponse ranking = rankingService.getFriendStreakRanking(username);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Statistiken eines bestimmten Users (nur für Freunde sichtbar)
     */
    @GetMapping("/user/{targetUsername}/stats")
    public ResponseEntity<?> getUserStats(
            @RequestHeader("Authorization") String token,
            @PathVariable String targetUsername) {
        try {
            String currentUsername = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));

            // Prüfe ob sie Freunde sind oder der gleiche User
            if (!currentUsername.equals(targetUsername)) {
                // TODO: Freundschafts-Check implementieren
                // if (!friendshipService.areFriends(currentUsername, targetUsername)) {
                //     return ResponseEntity.status(403).body(Map.of("error", "Du kannst nur Statistiken von Freunden einsehen"));
                // }
            }

            UserStats stats = rankingService.calculateUserStats(targetUsername);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Leaderboard mit verschiedenen Kategorien
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "trainings") String category,
            @RequestParam(defaultValue = "global") String scope,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));

            RankingResponse ranking;

            switch (category.toLowerCase()) {
                case "trainings":
                case "total":
                    ranking = rankingService.getGlobalTrainingRanking(username, limit);
                    break;
                case "monthly":
                    ranking = rankingService.getMonthlyTrainingRanking(username, limit);
                    break;
                case "streaks":
                case "streak":
                    ranking = rankingService.getStreakRanking(username, limit);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Unbekannte Kategorie: " + category));
            }

            return ResponseEntity.ok(Map.of(
                    "category", category,
                    "scope", scope,
                    "ranking", ranking
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ranking-Zusammenfassung (Dashboard-Widget)
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getRankingSummary(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            UserStats userStats = rankingService.calculateUserStats(username);

            // Top 3 Rankings für Quick View
            RankingResponse globalTop3 = rankingService.getGlobalTrainingRanking(username, 3);
            RankingResponse monthlyTop3 = rankingService.getMonthlyTrainingRanking(username, 3);
            RankingResponse streakTop3 = rankingService.getStreakRanking(username, 3);

            return ResponseEntity.ok(Map.of(
                    "myStats", userStats,
                    "topTrainings", globalTop3.getEntries(),
                    "topMonthly", monthlyTop3.getEntries(),
                    "topStreaks", streakTop3.getEntries(),
                    "myPositions", Map.of(
                            "global", globalTop3.getCurrentUserPosition(),
                            "monthly", monthlyTop3.getCurrentUserPosition(),
                            "streak", streakTop3.getCurrentUserPosition()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Achievements und Milestones
     */
    @GetMapping("/achievements")
    public ResponseEntity<?> getAchievements(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            RankingOverviewResponse overview = rankingService.getRankingOverview(username);

            return ResponseEntity.ok(Map.of(
                    "achievements", overview.getRecentAchievements(),
                    "milestones", overview.getUpcomingMilestones(),
                    "userStats", overview.getCurrentUserStats()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ranking History (für Charts/Trends) - Future Feature
     */
    @GetMapping("/history")
    public ResponseEntity<?> getRankingHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "monthly") String period) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token.replace("Bearer ", ""));

            // TODO: Implementiere Ranking History
            // Für jetzt nur aktuelle Stats zurückgeben
            UserStats currentStats = rankingService.calculateUserStats(username);

            return ResponseEntity.ok(Map.of(
                    "message", "Ranking History Feature kommt bald!",
                    "currentStats", currentStats,
                    "period", period
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}