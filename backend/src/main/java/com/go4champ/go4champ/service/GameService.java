package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.Game;
import com.go4champ.go4champ.model.User;
import com.go4champ.go4champ.repo.GameRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {
    @Autowired
    private GameRepo repo;

    @Autowired
    private UserService userService;  // Für die Verwaltung von User-Objekten

    public List<Game> getAllGames() {
        return repo.findAll();
    }

    public Game getOneGame(int gameId) {
        return repo.findById(gameId).orElse(null);
    }

    @Transactional
    public Game createGame(Game game) {
        // Wenn ein User gesetzt ist, stellen wir sicher, dass er persistiert ist
        if (game.getUser() != null) {
            User user = userService.getUserById(game.getUser().getUsername());
            if (user != null) {
                // Sicherstellen, dass wir einen gespeicherten User verwenden
                game.setUser(user);
            } else {
                // Wenn der User noch nicht existiert, speichern wir ihn zuerst
                user = userService.createUser(game.getUser());
                game.setUser(user);
            }
        }
        return repo.save(game);
    }

    @Transactional
    public void deleteById(int gameId) {
        Game game = getOneGame(gameId);
        if (game != null && game.getUser() != null) {
            User user = game.getUser();
            // User-Game-Beziehung auflösen
            user.setGame(null);
            userService.updateUser(user);
        }
        repo.deleteById(gameId);
    }

    @Transactional
    public Game assignUserToGame(int gameId, String username) {
        Game game = getOneGame(gameId);
        User user = userService.getUserById(username);

        if (game != null && user != null) {
            // Existierende Game-Beziehung entfernen, falls vorhanden
            if (user.getGame() != null && user.getGame().getGameId() != gameId) {
                Game oldGame = user.getGame();
                oldGame.setUser(null);
                repo.save(oldGame);
            }

            // Bestehende User-Beziehung des Games entfernen, falls vorhanden
            if (game.getUser() != null && !game.getUser().getUsername().equals(username)) {
                User oldUser = game.getUser();
                oldUser.setGame(null);
                userService.updateUser(oldUser);
            }

            // Neue Beziehung setzen
            game.setUser(user);
            user.setGame(game);

            // Erst User speichern, dann Game
            userService.updateUser(user);
            return repo.save(game);
        }
        return null;
    }
}