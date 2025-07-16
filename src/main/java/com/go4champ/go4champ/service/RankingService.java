package com.go4champ.go4champ.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.go4champ.go4champ.dto.*;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {

    @Autowired
    private TrainingRepo trainingRepo;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private ChallengeRepository challengeRepository;

    // =============================================================================
    // USER STATS BERECHNUNG - EMERGENCY FIX (NO INFINITE LOOP)
    // =============================================================================

    /**
     * EMERGENCY FIX: Berechnet alle Statistiken für einen User (OHNE Ranking Positions!)
     */
    public UserStats calculateUserStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden: " + username));

        UserStats stats = new UserStats(username, user.getName());
        stats.setAvatarId(user.getAvatarID());

        // Training Statistiken
        stats.setTotalTrainings(trainingRepo.countByUserUsername(username));
        stats.setMonthlyTrainings(getCurrentMonthTrainingCount(username));
        stats.setWeeklyTrainings(getCurrentWeekTrainingCount(username));
        stats.setTodayTrainings(getTodayTrainingCount(username));

        // Challenge Statistiken
        stats.setTotalChallenges(challengeRepository.findAllByUsername(username).size());
        stats.setWonChallenges(challengeRepository.countWonChallenges(username));
        stats.setLostChallenges(challengeRepository.countLostChallenges(username));

        if (stats.getTotalChallenges() > 0) {
            stats.setChallengeWinRate((double) stats.getWonChallenges() / stats.getTotalChallenges() * 100);
        }

        // Streak Berechnung
        calculateStreaks(username, stats);

        // Consistency Score
        stats.setConsistencyScore(calculateConsistencyScore(username));

        // Training Qualität
        Double avgDifficulty = trainingRepo.findAverageTrainingDifficultyByUsername(username);
        stats.setAverageDifficulty(avgDifficulty != null ? avgDifficulty : 0.0);

        Double maxDifficulty = trainingRepo.findMaxDifficultyByUsername(username);
        stats.setMaxDifficulty(maxDifficulty != null ? maxDifficulty : 0.0);

        Long totalTime = trainingRepo.findTotalTrainingTimeByUsername(username);
        stats.setTotalTrainingTime(totalTime != null ? totalTime : 0L);

        Double avgTime = trainingRepo.findAverageTrainingDurationByUsername(username);
        stats.setAverageTrainingTime(avgTime != null ? avgTime : 0.0);

        // Activity Status
        stats.setHasTrainedToday(trainingRepo.hasTrainedToday(username));

        Optional<Training> lastTraining = trainingRepo.findLatestByUsername(username);
        if (lastTraining.isPresent()) {
            stats.setLastTrainingDate(lastTraining.get().getCreatedAt());
        }

        // Tage mit Aktivität
        List<LocalDate> activeDays = getDistinctTrainingDates(username);
        stats.setDaysActive(activeDays.size());

        // EMERGENCY FIX: KEINE Ranking Positions! (verhindert Infinite Loop)
        // Diese werden separat berechnet, wenn sie benötigt werden

        return stats;
    }

    /**
     * NEUE METHODE: Berechnet UserStats MIT Ranking Positions (nur wenn explizit gewünscht)
     */
    public UserStats calculateUserStatsWithRankings(String username) {
        UserStats stats = calculateUserStats(username); // Basis-Stats ohne Rankings

        // Jetzt Rankings hinzufügen (vorsichtig, um Loops zu vermeiden)
        try {
            stats.setOverallRank(calculateUserRankPosition(username, "TOTAL"));
            stats.setMonthlyRank(calculateUserRankPosition(username, "MONTHLY"));
            stats.setStreakRank(calculateUserRankPosition(username, "STREAK"));
        } catch (Exception e) {
            System.err.println("Fehler beim Berechnen der Ranking-Positionen für " + username + ": " + e.getMessage());
            // Setze Default-Werte
            stats.setOverallRank(0);
            stats.setMonthlyRank(0);
            stats.setStreakRank(0);
        }

        return stats;
    }

    /**
     * NEUE METHODE: Berechnet Ranking-Position für einen User (ohne Infinite Loop)
     */
    private int calculateUserRankPosition(String username, String rankingType) {
        switch (rankingType) {
            case "TOTAL":
                List<Object[]> totalResults = trainingRepo.findUsersWithMostTrainings();
                for (int i = 0; i < totalResults.size(); i++) {
                    if (totalResults.get(i)[0].equals(username)) {
                        return i + 1;
                    }
                }
                break;

            case "MONTHLY":
                LocalDateTime now = LocalDateTime.now();
                List<Object[]> monthlyResults = trainingRepo.findUsersWithMostTrainingsThisMonth(
                        now.getYear(), now.getMonthValue());
                for (int i = 0; i < monthlyResults.size(); i++) {
                    if (monthlyResults.get(i)[0].equals(username)) {
                        return i + 1;
                    }
                }
                break;

            case "STREAK":
                // Vereinfachte Streak-Position ohne Rekursion
                List<User> allUsers = userRepository.findAll();
                List<UserStreakSimple> userStreaks = new ArrayList<>();

                for (User user : allUsers) {
                    // Berechne nur den Streak, nicht die kompletten Stats
                    int streak = calculateUserStreak(user.getUsername());
                    userStreaks.add(new UserStreakSimple(user.getUsername(), streak));
                }

                userStreaks.sort((a, b) -> Integer.compare(b.streak, a.streak));

                for (int i = 0; i < userStreaks.size(); i++) {
                    if (userStreaks.get(i).username.equals(username)) {
                        return i + 1;
                    }
                }
                break;
        }
        return 0; // Fallback
    }

    /**
     * NEUE METHODE: Berechnet nur den Streak für einen User (ohne komplette Stats)
     */
    private int calculateUserStreak(String username) {
        List<LocalDate> trainingDates = getDistinctTrainingDates(username);

        if (trainingDates.isEmpty()) {
            return 0;
        }

        int currentStreak = 0;
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        // Prüfe ob heute trainiert wurde
        if (trainingDates.contains(today)) {
            currentStreak = 1;
            checkDate = today.minusDays(1);
        } else if (trainingDates.contains(today.minusDays(1))) {
            currentStreak = 1;
            checkDate = today.minusDays(2);
        } else {
            return 0; // Kein aktueller Streak
        }

        // Weitere Streak-Tage prüfen
        for (LocalDate trainingDate : trainingDates) {
            if (trainingDate.equals(checkDate)) {
                currentStreak++;
                checkDate = checkDate.minusDays(1);
            } else if (trainingDate.isBefore(checkDate)) {
                break;
            }
        }

        return currentStreak;
    }

    // =============================================================================
    // STREAK BERECHNUNG - FIXED VERSION
    // =============================================================================

    /**
     * FIXED: Holt alle unterschiedlichen Trainings-Daten für einen User
     */
    private List<LocalDate> getDistinctTrainingDates(String username) {
        List<Training> trainings = trainingRepo.findByUsernameOrderByDateDesc(username);
        return trainings.stream()
                .map(training -> training.getCreatedAt().toLocalDate())
                .distinct()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * FIXED: Berechnet Current Streak und Longest Streak
     */
    private void calculateStreaks(String username, UserStats stats) {
        List<LocalDate> trainingDates = getDistinctTrainingDates(username);

        if (trainingDates.isEmpty()) {
            stats.setCurrentStreak(0);
            stats.setLongestStreak(0);
            return;
        }

        // Current Streak berechnen
        int currentStreak = calculateUserStreak(username);
        stats.setCurrentStreak(currentStreak);

        // Longest Streak berechnen
        stats.setLongestStreak(Math.max(currentStreak, calculateLongestStreak(trainingDates)));
    }

    /**
     * FIXED: Berechnet den längsten Streak
     */
    private int calculateLongestStreak(List<LocalDate> trainingDates) {
        if (trainingDates.isEmpty()) return 0;

        List<LocalDate> sortedDates = trainingDates.stream()
                .sorted()
                .collect(Collectors.toList());

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate prevDate = sortedDates.get(i - 1);
            LocalDate currDate = sortedDates.get(i);

            if (currDate.equals(prevDate.plusDays(1))) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak;
    }

    // =============================================================================
    // CONSISTENCY SCORE BERECHNUNG
    // =============================================================================

    private double calculateConsistencyScore(String username) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Training> recentTrainings = trainingRepo.findByUsernameAfterDate(username, thirtyDaysAgo);

        if (recentTrainings.isEmpty()) {
            return 0.0;
        }

        Map<LocalDate, Long> trainingsByDate = recentTrainings.stream()
                .collect(Collectors.groupingBy(
                        training -> training.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        int daysWithTraining = trainingsByDate.size();
        double dayScore = (daysWithTraining / 30.0) * 60;

        int currentStreak = calculateUserStreak(username);
        double streakBonus = Math.min(currentStreak * 2, 30);

        double avgTrainingsPerDay = recentTrainings.size() / 30.0;
        double frequencyBonus = Math.min(avgTrainingsPerDay * 10, 10);

        return Math.min(dayScore + streakBonus + frequencyBonus, 100.0);
    }

    // =============================================================================
    // HELPER METHODEN FÜR ZEITRÄUME
    // =============================================================================

    private long getCurrentMonthTrainingCount(String username) {
        LocalDateTime now = LocalDateTime.now();
        return trainingRepo.countByUsernameForMonth(username, now.getYear(), now.getMonthValue());
    }

    private long getCurrentWeekTrainingCount(String username) {
        LocalDateTime now = LocalDateTime.now();
        int weekOfYear = now.getDayOfYear() / 7 + 1;
        return trainingRepo.countByUsernameForWeek(username, now.getYear(), weekOfYear);
    }

    private long getTodayTrainingCount(String username) {
        return trainingRepo.countByUsernameForDate(username, LocalDate.now());
    }

    // =============================================================================
    // GLOBALE RANKINGS
    // =============================================================================

    public RankingResponse getGlobalTrainingRanking(String currentUsername, int limit) {
        List<Object[]> results = trainingRepo.findUsersWithMostTrainings();

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;

        for (Object[] result : results) {
            if (position > limit) break;

            String username = (String) result[0];
            Long trainingCount = (Long) result[1];

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) continue;

            RankingEntry entry = new RankingEntry(position, username, user.getName(),
                    trainingCount, trainingCount + " Trainings");
            entry.setAvatarId(user.getAvatarID());
            entry.setBadge(getBadgeForPosition(position));
            entry.setCurrentUser(username.equals(currentUsername));

            entries.add(entry);
            position++;
        }

        RankingResponse response = new RankingResponse("TOTAL_TRAININGS", "Gesamte Trainings", entries);
        response.setPeriod("Allzeit");
        response.setDescription("Ranking nach der Gesamtanzahl aller Trainings");
        response.setUpdateFrequency("Echtzeit");

        findCurrentUserPosition(response, currentUsername, results);

        return response;
    }

    public RankingResponse getMonthlyTrainingRanking(String currentUsername, int limit) {
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> results = trainingRepo.findUsersWithMostTrainingsThisMonth(
                now.getYear(), now.getMonthValue());

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;

        for (Object[] result : results) {
            if (position > limit) break;

            String username = (String) result[0];
            Long trainingCount = (Long) result[1];

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) continue;

            RankingEntry entry = new RankingEntry(position, username, user.getName(),
                    trainingCount, trainingCount + " Trainings diesen Monat");
            entry.setAvatarId(user.getAvatarID());
            entry.setBadge(getBadgeForPosition(position));
            entry.setCurrentUser(username.equals(currentUsername));

            entries.add(entry);
            position++;
        }

        RankingResponse response = new RankingResponse("MONTHLY_TRAININGS", "Trainings diesen Monat", entries);
        response.setPeriod(now.getMonth().name() + " " + now.getYear());
        response.setDescription("Ranking nach Trainings im aktuellen Monat");
        response.setUpdateFrequency("Echtzeit");

        findCurrentUserPosition(response, currentUsername, results);

        return response;
    }

    /**
     * FIXED: Streak Ranking (ohne Infinite Loop)
     */
    public RankingResponse getStreakRanking(String currentUsername, int limit) {
        List<User> allUsers = userRepository.findAll();

        List<RankingEntry> entries = new ArrayList<>();

        // FIXED: Berechne Streaks direkt, ohne calculateUserStats() aufzurufen
        List<UserStreakInfo> userStreaks = new ArrayList<>();
        for (User user : allUsers) {
            int streak = calculateUserStreak(user.getUsername()); // Nur Streak, keine kompletten Stats
            userStreaks.add(new UserStreakInfo(user, streak));
        }

        userStreaks.sort((a, b) -> Integer.compare(b.currentStreak, a.currentStreak));

        int position = 1;
        for (UserStreakInfo userStreak : userStreaks) {
            if (position > limit) break;

            User user = userStreak.user;
            int streak = userStreak.currentStreak;

            RankingEntry entry = new RankingEntry(position, user.getUsername(), user.getName(),
                    streak, streak + " Tage Streak");
            entry.setAvatarId(user.getAvatarID());
            entry.setBadge(getStreakBadge(streak));
            entry.setCurrentUser(user.getUsername().equals(currentUsername));

            if (streak > 0) {
                entry.setAdditionalInfo("🔥 Aktiv");
            }

            entries.add(entry);
            position++;
        }

        RankingResponse response = new RankingResponse("STREAK", "Training Streaks", entries);
        response.setPeriod("Aktuell");
        response.setDescription("Ranking nach aktuellen Training-Streaks");
        response.setUpdateFrequency("Täglich");

        for (int i = 0; i < userStreaks.size(); i++) {
            if (userStreaks.get(i).user.getUsername().equals(currentUsername)) {
                response.setCurrentUserPosition(i + 1);
                response.setCurrentUserInTopList(i < limit);
                break;
            }
        }

        return response;
    }

    // =============================================================================
    // FRIEND RANKINGS - FIXED VERSION (NO LOB STREAM PROBLEM)
    // =============================================================================

    /**
     * FIXED: Friend Ranking für monatliche Trainings (ohne LOB stream Problem)
     */
    public FriendRankingResponse getFriendMonthlyRanking(String username) {
        List<User> friends = friendshipService.getFriends(username);
        LocalDateTime now = LocalDateTime.now();

        List<RankingEntry> friendEntries = new ArrayList<>();

        List<FriendTrainingInfo> friendInfos = new ArrayList<>();
        for (User friend : friends) {
            long monthlyTrainings = trainingRepo.countByUsernameForMonth(
                    friend.getUsername(), now.getYear(), now.getMonthValue());
            friendInfos.add(new FriendTrainingInfo(friend, monthlyTrainings));
        }

        friendInfos.sort((a, b) -> Long.compare(b.monthlyTrainings, a.monthlyTrainings));

        int position = 1;
        for (FriendTrainingInfo friendInfo : friendInfos) {
            User friend = friendInfo.user;
            long trainings = friendInfo.monthlyTrainings;

            // FIXED: Erstelle RankingEntry nur mit einfachen Daten, keine User-Objekte
            RankingEntry entry = new RankingEntry(position, friend.getUsername(), friend.getName(),
                    trainings, trainings + " Trainings");
            entry.setAvatarId(friend.getAvatarID());
            entry.setBadge(getBadgeForPosition(position));

            friendEntries.add(entry);
            position++;
        }

        long userMonthlyTrainings = trainingRepo.countByUsernameForMonth(
                username, now.getYear(), now.getMonthValue());
        User currentUser = userRepository.findByUsername(username).orElse(null);

        // FIXED: Erstelle currentUserEntry nur mit einfachen Daten
        RankingEntry currentUserEntry = new RankingEntry(0, username, currentUser.getName(),
                userMonthlyTrainings, userMonthlyTrainings + " Trainings");
        currentUserEntry.setAvatarId(currentUser.getAvatarID());
        currentUserEntry.setCurrentUser(true);

        int userPosition = 1;
        for (FriendTrainingInfo friendInfo : friendInfos) {
            if (friendInfo.monthlyTrainings > userMonthlyTrainings) {
                userPosition++;
            }
        }
        currentUserEntry.setPosition(userPosition);

        FriendRankingResponse response = new FriendRankingResponse(
                "FRIEND_MONTHLY", "Freunde - Diesen Monat", friendEntries, currentUserEntry);
        response.setPeriod(now.getMonth().name() + " " + now.getYear());

        return response;
    }

    /**
     * FIXED: Friend Ranking für Streaks (ohne LOB stream Problem)
     */
    public FriendRankingResponse getFriendStreakRanking(String username) {
        List<User> friends = friendshipService.getFriends(username);

        List<RankingEntry> friendEntries = new ArrayList<>();

        // FIXED: Direkte Streak-Berechnung ohne komplette UserStats
        List<FriendStreakInfo> friendInfos = new ArrayList<>();
        for (User friend : friends) {
            int streak = calculateUserStreak(friend.getUsername());
            friendInfos.add(new FriendStreakInfo(friend, streak));
        }

        // Aktueller User Streak
        int userStreak = calculateUserStreak(username);
        User currentUser = userRepository.findByUsername(username).orElse(null);

        // Alle User für korrektes Ranking
        List<FriendStreakInfo> allInfos = new ArrayList<>(friendInfos);
        allInfos.add(new FriendStreakInfo(currentUser, userStreak));

        allInfos.sort((a, b) -> Integer.compare(b.currentStreak, a.currentStreak));

        int position = 1;
        int currentUserPosition = 1;

        for (FriendStreakInfo info : allInfos) {
            User user = info.user;
            int streak = info.currentStreak;

            if (!user.getUsername().equals(username)) {
                // FIXED: Erstelle RankingEntry nur mit einfachen Daten, keine User-Objekte
                RankingEntry entry = new RankingEntry(position, user.getUsername(), user.getName(),
                        streak, streak + " Tage");
                entry.setAvatarId(user.getAvatarID());
                entry.setBadge(getStreakBadge(streak));

                if (streak > 0) {
                    entry.setAdditionalInfo("🔥");
                }

                friendEntries.add(entry);
            } else {
                currentUserPosition = position;
            }
            position++;
        }

        // FIXED: Erstelle currentUserEntry nur mit einfachen Daten
        RankingEntry currentUserEntry = new RankingEntry(currentUserPosition, username, currentUser.getName(),
                userStreak, userStreak + " Tage");
        currentUserEntry.setAvatarId(currentUser.getAvatarID());
        currentUserEntry.setCurrentUser(true);
        currentUserEntry.setBadge(getStreakBadge(userStreak));

        FriendRankingResponse response = new FriendRankingResponse(
                "FRIEND_STREAK", "Freunde - Streaks", friendEntries, currentUserEntry);
        response.setPeriod("Aktuell");

        return response;
    }

    // =============================================================================
    // RANKING OVERVIEW
    // =============================================================================

    /**
     * COMPLETELY FIXED: Ranking Overview (ohne LOB stream Problem)
     */
    public RankingOverviewResponse getRankingOverview(String username) {
        try {
            RankingOverviewResponse overview = new RankingOverviewResponse();

            // FIXED: Verwende nur calculateUserStats (OHNE Rankings, um Loops zu vermeiden)
            UserStats userStats = calculateUserStats(username);
            overview.setCurrentUserStats(userStats);

            // FIXED: Verwende EINFACHE Ranking-Listen ohne komplexe Objekte
            overview.setTopTrainingsAllTime(getSimpleGlobalRanking(username, 3));
            overview.setTopTrainingsThisMonth(getSimpleMonthlyRanking(username, 3));
            overview.setTopStreaks(getSimpleStreakRanking(username, 3));

            overview.setTopChallengeWinners(getSimpleChallengeRanking(username, 3));

            // FIXED: Verwende die bereits funktionierenden Friend-Rankings
            overview.setFriendsThisMonth(getFriendMonthlyRanking(username));
            overview.setFriendsStreak(getFriendStreakRanking(username));

            // FIXED: Berechne Positionen direkt (ohne komplexe Rankings)
            overview.setMyOverallPosition(calculateDirectPosition(username, "TOTAL"));
            overview.setMyMonthlyPosition(calculateDirectPosition(username, "MONTHLY"));
            overview.setMyStreakPosition(calculateDirectPosition(username, "STREAK"));

            // FIXED: Einfache Achievements ohne komplexe Berechnungen
            overview.setRecentAchievements(generateSimpleAchievements(userStats));
            overview.setUpcomingMilestones(generateSimpleMilestones(userStats));

            return overview;
        } catch (Exception e) {
            System.err.println("Error in getRankingOverview: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch ranking overview: " + e.getMessage());
        }
    }

    // NEUE HELPER-METHODEN (ohne LOB stream Probleme)

    private List<RankingEntry> getSimpleGlobalRanking(String currentUsername, int limit) {
        List<Object[]> results = trainingRepo.findUsersWithMostTrainings();
        List<RankingEntry> entries = new ArrayList<>();

        int position = 1;
        for (Object[] result : results) {
            if (position > limit) break;

            String username = (String) result[0];
            Long trainingCount = (Long) result[1];

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) continue;

            RankingEntry entry = new RankingEntry(position, username, user.getName(),
                    trainingCount, trainingCount + " Trainings");
            entry.setAvatarId(user.getAvatarID());
            entry.setBadge(getBadgeForPosition(position));
            entry.setCurrentUser(username.equals(currentUsername));

            entries.add(entry);
            position++;
        }

        return entries;
    }

    private List<RankingEntry> getSimpleMonthlyRanking(String currentUsername, int limit) {
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> results = trainingRepo.findUsersWithMostTrainingsThisMonth(
                now.getYear(), now.getMonthValue());
        List<RankingEntry> entries = new ArrayList<>();

        int position = 1;
        for (Object[] result : results) {
            if (position > limit) break;

            String username = (String) result[0];
            Long trainingCount = (Long) result[1];

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) continue;

            RankingEntry entry = new RankingEntry(position, username, user.getName(),
                    trainingCount, trainingCount + " Trainings");
            entry.setAvatarId(user.getAvatarID());
            entry.setBadge(getBadgeForPosition(position));
            entry.setCurrentUser(username.equals(currentUsername));

            entries.add(entry);
            position++;
        }

        return entries;
    }

    private List<RankingEntry> getSimpleStreakRanking(String currentUsername, int limit) {
        List<User> allUsers = userRepository.findAll();
        List<SimpleUserStreak> userStreaks = new ArrayList<>();

        for (User user : allUsers) {
            int streak = calculateUserStreak(user.getUsername());
            userStreaks.add(new SimpleUserStreak(user.getUsername(), user.getName(),
                    user.getAvatarID(), streak));
        }

        userStreaks.sort((a, b) -> Integer.compare(b.streak, a.streak));

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;

        for (SimpleUserStreak userStreak : userStreaks) {
            if (position > limit) break;

            RankingEntry entry = new RankingEntry(position, userStreak.username, userStreak.name,
                    userStreak.streak, userStreak.streak + " Tage");
            entry.setAvatarId(userStreak.avatarId);
            entry.setBadge(getStreakBadge(userStreak.streak));
            entry.setCurrentUser(userStreak.username.equals(currentUsername));

            entries.add(entry);
            position++;
        }

        return entries;
    }

    private List<RankingEntry> getSimpleChallengeRanking(String currentUsername, int limit) {
        List<User> allUsers = userRepository.findAll();
        List<SimpleChallengeWinner> challengeWinners = new ArrayList<>();

        for (User user : allUsers) {
            long wonChallenges = challengeRepository.countWonChallenges(user.getUsername());
            if (wonChallenges > 0) {
                challengeWinners.add(new SimpleChallengeWinner(user.getUsername(),
                        user.getName(), user.getAvatarID(), wonChallenges));
            }
        }

        challengeWinners.sort((a, b) -> Long.compare(b.wonChallenges, a.wonChallenges));

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;

        for (SimpleChallengeWinner winner : challengeWinners) {
            if (position > limit) break;

            RankingEntry entry = new RankingEntry(position, winner.username, winner.name,
                    winner.wonChallenges, winner.wonChallenges + " Siege");
            entry.setAvatarId(winner.avatarId);
            entry.setBadge(getBadgeForPosition(position));
            entry.setCurrentUser(winner.username.equals(currentUsername));

            entries.add(entry);
            position++;
        }

        return entries;
    }

    private int calculateDirectPosition(String username, String type) {
        switch (type) {
            case "TOTAL":
                List<Object[]> totalResults = trainingRepo.findUsersWithMostTrainings();
                for (int i = 0; i < totalResults.size(); i++) {
                    if (totalResults.get(i)[0].equals(username)) {
                        return i + 1;
                    }
                }
                break;

            case "MONTHLY":
                LocalDateTime now = LocalDateTime.now();
                long userMonthly = trainingRepo.countByUsernameForMonth(
                        username, now.getYear(), now.getMonthValue());
                List<Object[]> monthlyResults = trainingRepo.findUsersWithMostTrainingsThisMonth(
                        now.getYear(), now.getMonthValue());
                for (int i = 0; i < monthlyResults.size(); i++) {
                    if (monthlyResults.get(i)[0].equals(username)) {
                        return i + 1;
                    }
                }
                break;

            case "STREAK":
                int userStreak = calculateUserStreak(username);
                List<User> allUsers = userRepository.findAll();
                int position = 1;
                for (User user : allUsers) {
                    if (!user.getUsername().equals(username)) {
                        int otherStreak = calculateUserStreak(user.getUsername());
                        if (otherStreak > userStreak) {
                            position++;
                        }
                    }
                }
                return position;
        }
        return 1;
    }

    private List<String> generateSimpleAchievements(UserStats stats) {
        List<String> achievements = new ArrayList<>();

        if (stats.getCurrentStreak() >= 7) {
            achievements.add("🔥 7-Tage-Streak erreicht!");
        }
        if (stats.getTotalTrainings() >= 50) {
            achievements.add("💪 50 Trainings absolviert!");
        }
        if (stats.getMonthlyTrainings() >= 20) {
            achievements.add("⚡ 20 Trainings diesen Monat!");
        }

        if (achievements.isEmpty()) {
            achievements.add("🎯 Ranking-System aktiv!");
        }

        return achievements;
    }

    private List<String> generateSimpleMilestones(UserStats stats) {
        List<String> milestones = new ArrayList<>();

        if (stats.getCurrentStreak() < 7) {
            milestones.add("Noch " + (7 - stats.getCurrentStreak()) + " Tage bis zur 7-Tage-Streak");
        }
        if (stats.getTotalTrainings() < 100) {
            milestones.add("Noch " + (100 - stats.getTotalTrainings()) + " Trainings bis zu 100 Trainings");
        }

        if (milestones.isEmpty()) {
            milestones.add("Freunde hinzufügen für mehr Rankings!");
        }

        return milestones;
    }

    // Neue einfache Datenklassen
    private static class SimpleUserStreak {
        public final String username;
        public final String name;
        public final String avatarId;
        public final int streak;

        public SimpleUserStreak(String username, String name, String avatarId, int streak) {
            this.username = username;
            this.name = name;
            this.avatarId = avatarId;
            this.streak = streak;
        }
    }

    private static class SimpleChallengeWinner {
        public final String username;
        public final String name;
        public final String avatarId;
        public final long wonChallenges;

        public SimpleChallengeWinner(String username, String name, String avatarId, long wonChallenges) {
            this.username = username;
            this.name = name;
            this.avatarId = avatarId;
            this.wonChallenges = wonChallenges;
        }
    }

    // =============================================================================
    // HELPER METHODEN
    // =============================================================================

    private String getBadgeForPosition(int position) {
        switch (position) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "🏃";
        }
    }

    private String getStreakBadge(int streak) {
        if (streak >= 30) return "🔥";
        if (streak >= 14) return "💪";
        if (streak >= 7) return "⚡";
        if (streak >= 3) return "👍";
        return "🆕";
    }

    private void findCurrentUserPosition(RankingResponse response, String username, List<Object[]> results) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i)[0].equals(username)) {
                response.setCurrentUserPosition(i + 1);
                response.setCurrentUserInTopList(i < response.getEntries().size());
                return;
            }
        }
        response.setCurrentUserPosition(-1);
        response.setCurrentUserInTopList(false);
    }

    private List<RankingEntry> getChallengeWinnerRanking(String currentUsername, int limit) {
        List<RankingEntry> entries = new ArrayList<>();

        List<User> allUsers = userRepository.findAll();
        List<UserChallengeInfo> userChallenges = new ArrayList<>();

        for (User user : allUsers) {
            long wonChallenges = challengeRepository.countWonChallenges(user.getUsername());
            if (wonChallenges > 0) {
                userChallenges.add(new UserChallengeInfo(user, wonChallenges));
            }
        }

        userChallenges.sort((a, b) -> Long.compare(b.wonChallenges, a.wonChallenges));

        int position = 1;
        for (UserChallengeInfo userChallenge : userChallenges) {
            if (position > limit) break;

            User user = userChallenge.user;
            long wins = userChallenge.wonChallenges;

            RankingEntry entry = new RankingEntry(position, user.getUsername(), user.getName(),
                    wins, wins + " Siege");
            entry.setAvatarId(user.getAvatarID());
            entry.setBadge(getBadgeForPosition(position));
            entry.setCurrentUser(user.getUsername().equals(currentUsername));

            entries.add(entry);
            position++;
        }

        return entries;
    }

    private List<String> generateAchievements(UserStats stats) {
        List<String> achievements = new ArrayList<>();

        if (stats.getCurrentStreak() >= 7) {
            achievements.add("🔥 7-Tage-Streak erreicht!");
        }
        if (stats.getTotalTrainings() >= 50) {
            achievements.add("💪 50 Trainings absolviert!");
        }
        if (stats.getMonthlyTrainings() >= 20) {
            achievements.add("⚡ 20 Trainings diesen Monat!");
        }
        if (stats.getChallengeWinRate() >= 75) {
            achievements.add("🏆 75% Challenge-Gewinnrate!");
        }

        return achievements;
    }

    private List<String> generateMilestones(UserStats stats) {
        List<String> milestones = new ArrayList<>();

        if (stats.getCurrentStreak() < 7) {
            milestones.add("Noch " + (7 - stats.getCurrentStreak()) + " Tage bis zur 7-Tage-Streak");
        }
        if (stats.getTotalTrainings() < 100) {
            milestones.add("Noch " + (100 - stats.getTotalTrainings()) + " Trainings bis zu 100 Trainings");
        }
        if (stats.getMonthlyTrainings() < 30) {
            milestones.add("Noch " + (30 - stats.getMonthlyTrainings()) + " Trainings bis zu 30 Trainings diesen Monat");
        }

        return milestones;
    }

    // =============================================================================
    // HELPER KLASSEN
    // =============================================================================

    private static class UserStreakInfo {
        User user;
        int currentStreak;

        UserStreakInfo(User user, int currentStreak) {
            this.user = user;
            this.currentStreak = currentStreak;
        }
    }

    private static class UserStreakSimple {
        String username;
        int streak;

        UserStreakSimple(String username, int streak) {
            this.username = username;
            this.streak = streak;
        }
    }

    private static class FriendTrainingInfo {
        @JsonIgnore
        User user;
        long monthlyTrainings;

        FriendTrainingInfo(User user, long monthlyTrainings) {
            this.user = user;
            this.monthlyTrainings = monthlyTrainings;
        }
    }

    private static class FriendStreakInfo {
        @JsonIgnore

        User user;
        int currentStreak;

        FriendStreakInfo(User user, int currentStreak) {
            this.user = user;
            this.currentStreak = currentStreak;
        }
    }

    private static class UserChallengeInfo {
        @JsonIgnore

        User user;
        long wonChallenges;

        UserChallengeInfo(User user, long wonChallenges) {
            this.user = user;
            this.wonChallenges = wonChallenges;
        }
    }
}