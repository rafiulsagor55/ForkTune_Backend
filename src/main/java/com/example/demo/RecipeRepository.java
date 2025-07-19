package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.type.TypeReference;
//import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.ResultSet;
import java.util.*;

@Repository
public class RecipeRepository {

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	@Autowired
    private JdbcTemplate jdbcTemplatePure;

	public void saveImage(String imageId, byte[] data, String email) {
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

	public void save(Recipe recipe, String email) {
		String sql = "INSERT INTO recipes (title, email, image_id, description, prep_time, cook_time, calories, protein, fat, carbs, ingredients, instructions, meal_type, date) "
            + "VALUES (:title, :email, :imageId, :description, :prepTime, :cookTime, :calories, :protein, :fat, :carbs, :ingredients, :instructions, :mealType, :date)";

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

	public List<Recipe> findAllPublishedRecipe() {
		String sql = "SELECT * FROM recipes where flag=1";
		return jdbcTemplate.query(sql, new MapSqlParameterSource(), this::mapRowToRecipe);
	}

	public List<Recipe> findById(String email) {
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
			recipe.setRating(rs.getDouble("rating"));

			recipe.setIngredients(
					mapper.readValue(rs.getString("ingredients"), new TypeReference<List<Map<String, String>>>() {
					}));

			recipe.setInstructions(mapper.readValue(rs.getString("instructions"), new TypeReference<List<String>>() {
			}));

			String preferencesJson = rs.getString("preferences");
			if (preferencesJson != null && !preferencesJson.isEmpty()) {
				recipe.setPreferences(mapper.readValue(preferencesJson, new TypeReference<Map<String, Object>>() {
				}));
			} else {
				recipe.setPreferences(new HashMap<>()); 
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
	 	String sql = "UPDATE recipes SET title = :title, description = :description, meal_type = :mealType, "
	 			+ "prep_time = :prepTime, cook_time = :cookTime, calories = :calories, protein = :protein, "
	 			+ "fat = :fat, carbs = :carbs, ingredients = :ingredients, instructions = :instructions "
	 			+ "WHERE id = :recipeId";

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


//	public boolean updaterecipe(String recipeId, Recipe recipe) {
//    String sql = """
//        UPDATE recipes SET 
//            title = ?, 
//            description = ?, 
//            meal_type = ?, 
//            prep_time = ?, 
//            cook_time = ?, 
//            calories = ?, 
//            protein = ?, 
//            fat = ?, 
//            carbs = ?, 
//            ingredients = ?,   -- jsonb
//            instructions = ?   -- jsonb
//        WHERE id = ?
//    """;
//
//    try {
//        ObjectMapper mapper = new ObjectMapper();
//
//        // ✅ JSON string
//        String ingredientsJson = mapper.writeValueAsString(recipe.getIngredients());
//        String instructionsJson = mapper.writeValueAsString(recipe.getInstructions());
//
//        // ✅ PGobject for jsonb
//        PGobject ingredientsPg = new PGobject();
//        ingredientsPg.setType("jsonb");
//        ingredientsPg.setValue(ingredientsJson);
//
//        PGobject instructionsPg = new PGobject();
//        instructionsPg.setType("jsonb");
//        instructionsPg.setValue(instructionsJson);
//
//        int rowsUpdated = jdbcTemplatePure.update(
//                sql,
//                recipe.getTitle(),
//                recipe.getDescription(),
//                recipe.getMealType(),
//                recipe.getPrepTime(),
//                recipe.getCookTime(),
//                recipe.getCalories(),
//                recipe.getProtein(),
//                recipe.getFat(),
//                recipe.getCarbs(),
//                ingredientsPg,      // ✅ jsonb হিসেবে bind
//                instructionsPg,     // ✅ jsonb হিসেবে bind
//                recipeId
//        );
//
//        System.out.println("✅ Rows updated: " + rowsUpdated);
//        return rowsUpdated > 0;
//
//    } catch (Exception e) {
//        throw new RuntimeException("Failed to update recipe JSONB fields", e);
//    }
//}




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

//public void savePreferences(String recipeId, Map<String, Object> preferences) {
//    try {
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonPrefs = mapper.writeValueAsString(preferences);
//
//        PGobject prefsPg = new PGobject();
//        prefsPg.setType("jsonb");
//        prefsPg.setValue(jsonPrefs);
//
//        String sql = "UPDATE recipes SET preferences = ? WHERE id = ?";
//
//        jdbcTemplatePure.update(sql, prefsPg, recipeId);
//
//        System.out.println("✅ Preferences updated successfully!");
//
//    } catch (Exception e) {
//        throw new RuntimeException("Error saving preferences to DB", e);
//    }
//}






	public Boolean PublishRecipe(String id, int flag) {
		String sql = "UPDATE recipes SET flag = :flag " + "WHERE id = :id";
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
		params.put("flag", flag);
		System.out.println("Id: " + id);
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

	public boolean doesSavedItemExist(String email, String id) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM saved WHERE email = :email AND id = :id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("id", id);
		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

		return count > 0;
	}

	public void DeletesavedRecipee(String email, String id) {
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

	public Map<String, Object> getRecipeRating(String recipeId) {
		String sql = "SELECT AVG(rating) AS averageRating, COUNT(rating) AS count FROM recipe_ratings WHERE recipe_id = :recipeId";
		Map<String, Object> params = new HashMap<>();
		params.put("recipeId", recipeId);

		return jdbcTemplate.queryForMap(sql, params);
	}

	public Double getUserRating(String recipeId, String email) {
		String sql = "SELECT rating FROM recipe_ratings WHERE recipe_id = :recipeId AND email = :email";
		Map<String, Object> params = new HashMap<>();
		params.put("recipeId", recipeId);
		params.put("email", email);

		Double rating = jdbcTemplate.queryForObject(sql, params, Double.class);
		return rating != null ? rating : 0.0;
	}

	public void submitRating(String recipeId, Double rating, String email) {
		String sql = "INSERT INTO recipe_ratings (recipe_id, rating, email) " + "VALUES (:recipeId, :rating, :email) "
				+ "ON DUPLICATE KEY UPDATE rating = :rating";

		Map<String, Object> params = new HashMap<>();
		params.put("recipeId", recipeId);
		params.put("rating", rating);
		params.put("email", email);

		jdbcTemplate.update(sql, params);

		updateRecipeRating(recipeId);
	}
	
	
//	public void submitRating(String recipeId, Double rating, String email) {
//	    String sql = """
//	        INSERT INTO recipe_ratings (recipe_id, rating, email)
//	        VALUES (:recipeId, :rating, :email)
//	        ON CONFLICT (recipe_id, email) 
//	        DO UPDATE SET rating = EXCLUDED.rating
//	    """;
//
//	    Map<String, Object> params = new HashMap<>();
//	    params.put("recipeId", recipeId);
//	    params.put("rating", rating);
//	    params.put("email", email);
//
//	    jdbcTemplate.update(sql, params);
//
//	    updateRecipeRating(recipeId);
//	}


	public void deleteUserRating(String recipeId, String email) {
		String sql = "DELETE FROM recipe_ratings WHERE recipe_id = :recipeId AND email = :email";
		Map<String, Object> params = new HashMap<>();
		params.put("recipeId", recipeId);
		params.put("email", email);

		jdbcTemplate.update(sql, params);
		updateRecipeRating(recipeId);

	}

	private void updateRecipeRating(String recipeId) {
		String sql = "SELECT AVG(rating) AS averageRating FROM recipe_ratings WHERE recipe_id = :recipeId";
		Map<String, Object> params = new HashMap<>();
		params.put("recipeId", recipeId);

		Double averageRating = jdbcTemplate.queryForObject(sql, params, Double.class);
		if (averageRating != null) {
			String updateRecipeSql = "UPDATE recipes SET rating = :rating WHERE id = :recipeId";
			Map<String, Object> updateParams = new HashMap<>();
			updateParams.put("rating", averageRating);
			updateParams.put("recipeId", recipeId);
			jdbcTemplate.update(updateRecipeSql, updateParams);
		}
		else {
			String updateRecipeSql = "UPDATE recipes SET rating = 0 WHERE id = :recipeId";
			Map<String, Object> updateParams = new HashMap<>();
			updateParams.put("recipeId", recipeId);
			jdbcTemplate.update(updateRecipeSql, updateParams);
		}
	}
	
    public Map<String, String> getRecipeDetailsById(String recipeId) {
        String sql = "SELECT title, email FROM recipes WHERE id = :recipeId";
        Map<String, Object> params = new HashMap<>();
        params.put("recipeId", recipeId);
        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
            Map<String, String> result = new HashMap<>();
            result.put("title", rs.getString("title"));
            result.put("email", rs.getString("email"));
            return result;
        });
    }

}
