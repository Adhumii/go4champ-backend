package com.go4champ.go4champ.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "meal_ingredients", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "ingredient")
    private List<String> ingredients;

    @ElementCollection
    @CollectionTable(name = "meal_instructions", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "instruction")
    private List<String> instructions;

    private Integer protein;
    private Integer fat;
    private Integer carbs;

    @ManyToOne
    @JsonIgnore
    private NutritionPlan nutritionPlan;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

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

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }

    public Integer getProtein() { return protein; }
    public void setProtein(Integer protein) { this.protein = protein; }

    public Integer getFat() { return fat; }
    public void setFat(Integer fat) { this.fat = fat; }

    public Integer getCarbs() { return carbs; }
    public void setCarbs(Integer carbs) { this.carbs = carbs; }

    public NutritionPlan getNutritionPlan() { return nutritionPlan; }
    public void setNutritionPlan(NutritionPlan nutritionPlan) { this.nutritionPlan = nutritionPlan; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
