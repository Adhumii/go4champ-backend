package com.go4champ.go4champ.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.go4champ.go4champ.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
}