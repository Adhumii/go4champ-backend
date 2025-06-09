package com.go4champ.go4champ;

import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Go4ChampApplication {

	public static void main(String[] args) {
		SpringApplication.run(Go4ChampApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDefaultUser(UserService userService) {
		return args -> {
			if (!userService.existsByUsername("admin")) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword("adminPassword"); // KLARTEXT! UserService verschlüsselt es
				admin.setName("Admin User");
				admin.setRoles(List.of("ROLE_ADMIN"));

				userService.createUser(admin); // Hier wird das Passwort verschlüsselt
				System.out.println("Admin user created: admin / adminPassword");
			}

			// Zusätzlich einen normalen Testuser erstellen
			if (!userService.existsByUsername("testuser")) {
				User testUser = new User();
				testUser.setUsername("testuser");
				testUser.setPassword("testpassword"); // KLARTEXT! UserService verschlüsselt es
				testUser.setName("Test User");
				testUser.setAge(25);
				testUser.setWeight(75);
				testUser.setHeight(180);
				testUser.setWeightGoal(70);
				testUser.setRoles(List.of("ROLE_USER"));

				userService.createUser(testUser);
				System.out.println("Test user created: testuser / testpassword");
			}
		};
	}
}