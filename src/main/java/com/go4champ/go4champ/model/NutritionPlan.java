package com.go4champ.go4champ.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String planName;
    private LocalDate date;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "nutritionPlan", cascade = CascadeType.ALL)
    private List<Meal> meals = new ArrayList<>();
//wird gerade nicht gebraucht
//    public void addMeal(Meal meal) {
//        this.meals.add(meal);
//    }

    // Getter und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Meal> getMeals() { return meals; }
    public void setMeals(List<Meal> meals) { this.meals = meals; }
}

