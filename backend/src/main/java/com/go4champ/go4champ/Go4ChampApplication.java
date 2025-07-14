package com.go4champ.go4champ;

import com.go4champ.go4champ.model.*;
import com.go4champ.go4champ.service.UserService;
import com.go4champ.go4champ.service.FriendshipService;
import com.go4champ.go4champ.service.ChallengeService;
import com.go4champ.go4champ.dto.CreateChallengeRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class Go4ChampApplication {

	public static void main(String[] args) {
		SpringApplication.run(Go4ChampApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDefaultData(UserService userService, FriendshipService friendshipService, ChallengeService challengeService) {
		return args -> {
			// User erstellen
			if (!userService.existsByUsername("admin")) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword("adminPassword"); // KLARTEXT! UserService verschlüsselt es
				admin.setName("Admin User");
				admin.setEmail("admin@go4champ.com");
				admin.setEmailVerified(true);
				admin.setAge(30);
				admin.setGender("male");
				admin.setWeight(80);
				admin.setHeight(180);
				admin.setWeightGoal(75);
				admin.setAvatarID("admin_avatar");
				admin.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));

				userService.createUser(admin); // Hier wird das Passwort verschlüsselt
				System.out.println("Admin user created: admin / adminPassword");
			}

			// Zusätzlich einen normalen Testuser erstellen
			if (!userService.existsByUsername("testuser")) {
				User testUser = new User();
				testUser.setUsername("testuser");
				testUser.setPassword("testpassword"); // KLARTEXT! UserService verschlüsselt es
				testUser.setName("Test User");
				testUser.setEmail("test@go4champ.com");
				testUser.setEmailVerified(true);
				testUser.setAge(25);
				testUser.setGender("male");
				testUser.setWeight(75);
				testUser.setHeight(180);
				testUser.setWeightGoal(70);
				testUser.setAvatarID("test_avatar");
				testUser.setRoles(List.of("ROLE_USER"));

				userService.createUser(testUser);
				System.out.println("Test user created: testuser / testpassword");
			}

			// Freundschaft automatisch erstellen
			try {
				if (!friendshipService.areFriends("admin", "testuser")) {
					String result = friendshipService.sendFriendRequest("admin", "testuser", "Automatische Freundschaft für Tests");
					System.out.println("Friend request result: " + result);

					// Direkt akzeptieren (simuliert den Accept-Prozess)
					// Hier müssten wir eigentlich die Request-ID holen, aber für Demo-Zwecke:
					// friendshipService.acceptFriendRequest(requestId, "testuser");
					System.out.println("Users are now friends for testing");
				}
			} catch (Exception e) {
				System.out.println("Error creating friendship: " + e.getMessage());
			}

			// Test-Challenges erstellen
			try {
				// 1. REPS Challenge (PENDING)
				CreateChallengeRequest repsChallenge = new CreateChallengeRequest();
				repsChallenge.setChallengedUsername("testuser");
				repsChallenge.setType(ChallengeType.REPS);
				repsChallenge.setTitle("Push-up Challenge");
				repsChallenge.setDescription("Wer schafft mehr Liegestütze?");
				repsChallenge.setTargetValue(50.0);
				repsChallenge.setTargetUnit("Wiederholungen");
				repsChallenge.setDeadline(LocalDateTime.now().plusDays(7));

				challengeService.createChallenge("admin", repsChallenge);
				System.out.println("REPS Challenge created (PENDING)");

				// 2. WEIGHT Challenge (ACCEPTED)
				CreateChallengeRequest weightChallenge = new CreateChallengeRequest();
				weightChallenge.setChallengedUsername("admin");
				weightChallenge.setType(ChallengeType.WEIGHT);
				weightChallenge.setTitle("Bankdrücken Challenge");
				weightChallenge.setDescription("Wer drückt mehr Gewicht?");
				weightChallenge.setTargetValue(80.0);
				weightChallenge.setTargetUnit("kg");
				weightChallenge.setDeadline(LocalDateTime.now().plusDays(5));

				var acceptedChallenge = challengeService.createChallenge("testuser", weightChallenge);
				challengeService.acceptChallenge("admin", acceptedChallenge.getId());
				System.out.println("WEIGHT Challenge created and accepted");

				// 3. FREE Challenge (COMPLETED)
				CreateChallengeRequest freeChallenge = new CreateChallengeRequest();
				freeChallenge.setChallengedUsername("testuser");
				freeChallenge.setType(ChallengeType.FREE);
				freeChallenge.setTitle("Ausdauer Challenge");
				freeChallenge.setDescription("Wer läuft die längste Strecke?");
				freeChallenge.setDeadline(LocalDateTime.now().plusDays(3));

				var completedChallenge = challengeService.createChallenge("admin", freeChallenge);
				challengeService.acceptChallenge("testuser", completedChallenge.getId());
				System.out.println("FREE Challenge created, accepted and will be completed");

			} catch (Exception e) {
				System.out.println("Error creating test challenges: " + e.getMessage());
			}
		};
	}
}