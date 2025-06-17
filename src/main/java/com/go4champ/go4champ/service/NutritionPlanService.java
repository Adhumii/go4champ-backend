package com.go4champ.go4champ.service;

import com.go4champ.go4champ.model.NutritionPlan;
import com.go4champ.go4champ.repo.NutritionPlanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NutritionPlanService {

    @Autowired
    private NutritionPlanRepo nutritionPlanRepository;

    public NutritionPlan savePlan(NutritionPlan plan) {
        return nutritionPlanRepository.save(plan); // ✅ korrekter Typ
    }
}
