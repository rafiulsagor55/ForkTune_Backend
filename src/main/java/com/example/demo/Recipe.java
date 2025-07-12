package com.example.demo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class Recipe {
	private String id;
    private String title;
    private String imageId;
    private String description;
    private int prepTime;
    private int cookTime;
    private String calories;
    private String protein;
    private String fat;
    private String carbs;
    private List<Map<String, String>> ingredients;
    private List<String> instructions;
    private String mealType;
    private String date;
}
