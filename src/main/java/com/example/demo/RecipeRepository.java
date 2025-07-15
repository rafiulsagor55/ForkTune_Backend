package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class RecipeRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void saveImage(String imageId, byte[] data,String email) {
        String sql = "INSERT INTO recipe_images (image_id, email, image_data) VALUES (:imageId, :email, :imageData)";
        Map<String, Object> params = new HashMap<>();
        params.put("imageId", imageId);
        params.put("email", email);
        params.put("imageData", data);
        jdbcTemplate.update(sql, params);
    }

    public byte[] getImage(String imageId) {
        String sql = "SELECT image_data FROM recipe_images WHERE image_id = :imageId";
        Map<String, Object> params = Map.of("imageId", imageId);
        List<byte[]> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getBytes("image_data"));
        return results.isEmpty() ? null : results.get(0);
    }

    public void save(Recipe recipe,String email) {
        String sql = "INSERT INTO recipes (title, email, image_id, description, prep_time, cook_time, calories, protein, fat, carbs, ingredients, instructions, meal_type, date) " +
                     "VALUES (:title, :email, :imageId, :description, :prepTime, :cookTime, :calories, :protein, :fat, :carbs, :ingredients, :instructions, :mealType, :date)";

        Map<String, Object> params = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            params.put("title", recipe.getTitle());
            params.put("imageId", recipe.getImageId());
            params.put("description", recipe.getDescription());
            params.put("prepTime", recipe.getPrepTime());
            params.put("cookTime", recipe.getCookTime());
            params.put("calories", recipe.getCalories());
            params.put("protein", recipe.getProtein());
            params.put("fat", recipe.getFat());
            params.put("carbs", recipe.getCarbs());
            params.put("ingredients", mapper.writeValueAsString(recipe.getIngredients()));
            params.put("instructions", mapper.writeValueAsString(recipe.getInstructions()));
            params.put("mealType", recipe.getMealType());
            params.put("date", recipe.getDate());
            params.put("email", email);

            jdbcTemplate.update(sql, params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ingredients/instructions", e);
        }
    }
    
    
    public List<Recipe> findAll() {
        String sql = "SELECT * FROM recipes";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), this::mapRowToRecipe);
    }

    public List<Recipe>findById(String email) {
        String sql = "SELECT * FROM recipes WHERE email = :email";
        Map<String, Object> params = Map.of("email", email);
        List<Recipe> result = jdbcTemplate.query(sql, params, this::mapRowToRecipe);
        return result.isEmpty() ? null : result;
    }
    
   
    private Recipe mapRowToRecipe(ResultSet rs, int rowNum) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Recipe recipe = new Recipe();

            recipe.setId(rs.getString("id"));
            recipe.setFlag(rs.getInt("flag"));
            recipe.setTitle(rs.getString("title"));
            recipe.setImageId(rs.getString("image_id"));
            recipe.setDescription(rs.getString("description"));
            recipe.setPrepTime(rs.getInt("prep_time"));
            recipe.setCookTime(rs.getInt("cook_time"));
            recipe.setCalories(rs.getString("calories"));
            recipe.setProtein(rs.getString("protein"));
            recipe.setFat(rs.getString("fat"));
            recipe.setCarbs(rs.getString("carbs"));
            recipe.setMealType(rs.getString("meal_type"));
            recipe.setDate(rs.getString("date"));

            // Mapping ingredients
            recipe.setIngredients(mapper.readValue(rs.getString("ingredients"),
                    new TypeReference<List<Map<String, String>>>() {}));

            // Mapping instructions
            recipe.setInstructions(mapper.readValue(rs.getString("instructions"),
                    new TypeReference<List<String>>() {}));

            // Mapping preferences (check if it's null or empty)
            String preferencesJson = rs.getString("preferences");
            if (preferencesJson != null && !preferencesJson.isEmpty()) {
                recipe.setPreferences(mapper.readValue(preferencesJson,
                        new TypeReference<Map<String, Object>>() {}));
            } else {
                recipe.setPreferences(new HashMap<>());  // Set to an empty map if preferences is null or empty
            }

            return recipe;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping recipe", e);
        }
    }


    
    public boolean deleteRecipe(String recipeId) {
        String checkSql = "SELECT COUNT(*) FROM recipes WHERE id = :recipeId";
        Map<String, Object> params = new HashMap<>();
        params.put("recipeId", recipeId);
        Integer count = jdbcTemplate.queryForObject(checkSql, params, Integer.class);

        if (count != null && count > 0) {
            String deleteSql = "DELETE FROM recipes WHERE id = :recipeId";
            jdbcTemplate.update(deleteSql, params);
            return true;
        }
        return false;
    }
    
    public boolean updaterecipe(String recipeId, Recipe recipe) {
        String sql = "UPDATE recipes SET title = :title, description = :description, meal_type = :mealType, " +
                     "prep_time = :prepTime, cook_time = :cookTime, calories = :calories, protein = :protein, " +
                     "fat = :fat, carbs = :carbs, ingredients = :ingredients, instructions = :instructions " +
                     "WHERE id = :recipeId";

       

        Map<String, Object> params = new HashMap<>();
        
        try {
        	ObjectMapper mapper = new ObjectMapper();
            params.put("title", recipe.getTitle());
            params.put("description", recipe.getDescription());
            params.put("mealType", recipe.getMealType());
            params.put("prepTime", recipe.getPrepTime());
            params.put("cookTime", recipe.getCookTime());
            params.put("calories", recipe.getCalories());
            params.put("protein", recipe.getProtein());
            params.put("fat", recipe.getFat());
            params.put("carbs", recipe.getCarbs());
            params.put("ingredients", mapper.writeValueAsString(recipe.getIngredients()));
            params.put("instructions", mapper.writeValueAsString(recipe.getInstructions()));
            params.put("recipeId", recipeId);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize ingredients/instructions", e);
		}

        int rowsUpdated = jdbcTemplate.update(sql, params);

        return rowsUpdated > 0;
    }
    
    public void savePreferences(String recipeId, Map<String, Object> preferences) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonPrefs = mapper.writeValueAsString(preferences); // convert Map to JSON string

            String sql = "UPDATE recipes SET preferences = :preferences WHERE id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", recipeId);
            params.addValue("preferences", jsonPrefs);

            jdbcTemplate.update(sql, params);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving preferences to DB");
        }
    }
    
    public Boolean PublishRecipe(String id, int flag) {
    	String sql = "UPDATE recipes SET flag = :flag "+
                "WHERE id = :id";
    	Map<String, Object> params = new HashMap<>();
    	params.put("id", id);
    	params.put("flag", flag);
    	System.out.println("Id: "+id);
    	int rowsUpdated = jdbcTemplate.update(sql, params);
        return rowsUpdated > 0;
    	
    }
    
    public Boolean savedrecipe(String email, String id) {
    	String sql = "INSERT INTO saved (email, id) VALUES (:email, :id)";
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("id", id);
    	int rowsUpdated = jdbcTemplate.update(sql, params);
        return rowsUpdated > 0;
    	
    }
    
    public boolean doesSavedItemExist(String email,String id) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM saved WHERE email = :email AND id = :id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("id", id);
		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

		return count > 0;
	}
    
    public void DeletesavedRecipee(String email,String id) {
    	String checkSql = "DELETE FROM saved WHERE id = :id AND email = :email";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("email", email);
        jdbcTemplate.update(checkSql, params);
    }
    
    public List<Recipe> findSavedRecipesByEmail(String email) {
        String sql = "SELECT id FROM saved WHERE email = :email";
        Map<String, Object> params = Map.of("email", email);
        List<String> savedRecipeIds = jdbcTemplate.queryForList(sql, params, String.class);

        if (savedRecipeIds.isEmpty()) {
            return new ArrayList<>();
        }
        String recipeSql = "SELECT * FROM recipes WHERE id IN (:ids)";
        Map<String, Object> recipeParams = Map.of("ids", savedRecipeIds);
        return jdbcTemplate.query(recipeSql, recipeParams, this::mapRowToRecipe);
    }



}
