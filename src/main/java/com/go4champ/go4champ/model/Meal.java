package com.go4champ.go4champ.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;         // z. B. "Haferflocken mit Obst"
    private String type;         // Frühstück, Mittagessen etc.
    private int calories;

    @Lob
    private String description;

    @ManyToOne
    @JsonIgnore
    private NutritionPlan nutritionPlan;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public NutritionPlan getNutritionPlan() { return nutritionPlan; }
    public void setNutritionPlan(NutritionPlan nutritionPlan) { this.nutritionPlan = nutritionPlan; }
}
