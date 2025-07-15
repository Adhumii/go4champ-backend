package com.go4champ.go4champ.dto;

import java.util.List;

public class MealDto {

    private String name;
    private String type;
    private int calories;
    private String description;
    private List<String> ingredients;
    private List<String> instructions;
    private Integer protein;
    private Integer fat;
    private Integer carbs;

    public MealDto() {}

    public MealDto(String name, String type, int calories, String description,
                   List<String> ingredients, List<String> instructions,
                   Integer protein, Integer fat, Integer carbs) {
        this.name = name;
        this.type = type;
        this.calories = calories;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    // Getter und Setter

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
}
