package com.go4champ.go4champ.repo;

import com.go4champ.go4champ.model.Meal;
import com.go4champ.go4champ.model.NutritionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepo extends JpaRepository<Meal, Integer> {
}
