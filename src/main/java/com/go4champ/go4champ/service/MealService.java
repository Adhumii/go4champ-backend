package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.Meal;
import com.go4champ.go4champ.repo.MealRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MealService {

    @Autowired
    private MealRepo mealRepo;

    public Meal save(Meal meal) {
        return mealRepo.save(meal);
    }
}
