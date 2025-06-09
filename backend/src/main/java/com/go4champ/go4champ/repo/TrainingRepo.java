package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.Training;
import com.go4champ.go4champ.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepo extends JpaRepository<Training, Integer> {
    List<Training> findByUser(User user);
    List<Training> findByUserUsername(String username);
}