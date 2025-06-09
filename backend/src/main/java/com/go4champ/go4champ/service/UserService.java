package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUser() {
        return userRepo.findAll();
    }

    public User createUser(User user) {
        // Passwort verschlüsseln vor dem Speichern
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Standardrolle setzen falls nicht vorhanden
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("ROLE_USER"));
        }

        return userRepo.save(user);
    }

    public boolean delete(String username) {
        if (userRepo.existsById(username)) {
            try {
                userRepo.deleteById(username);
                return true;
            } catch (Exception e) {
                System.err.println("Fehler beim Löschen des Users: " + e.getMessage());
                return false;
            }
        }
        return false; // User existiert nicht
    }

    public User getUserById(String username) {
        Optional<User> user = userRepo.findById(username);
        return user.orElse(null);
    }

    public User updateUser(User user) {
        // Prüfen ob User existiert
        if (!userRepo.existsById(user.getUsername())) {
            return null;
        }

        // WICHTIG: Bestehendes Passwort beibehalten (nicht neu verschlüsseln)
        User existingUser = userRepo.findById(user.getUsername()).orElse(null);
        if (existingUser != null) {
            user.setPassword(existingUser.getPassword());
        }

        return userRepo.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findById(username);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsById(username);
    }

    // NEU: User nach E-Mail finden
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    // NEU: User nach Verification Token finden
    public User findByVerificationToken(String token) {
        List<User> allUsers = userRepo.findAll();
        return allUsers.stream()
                .filter(user -> token.equals(user.getVerificationToken()))
                .findFirst()
                .orElse(null);
    }

    // UserDetailsService Implementation für Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Rollen aus User-Entity verwenden
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}