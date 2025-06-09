package com.go4champ.go4champ.repo;
import com.go4champ.go4champ.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepo extends JpaRepository<Game, Integer> {
}
