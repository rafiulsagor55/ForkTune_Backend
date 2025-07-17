package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    public String storeImageInDB(MultipartFile file,String email) throws Exception {
        String imageId = UUID.randomUUID().toString();
        if(file.getContentType().startsWith("image")) {
        	byte[] imageData = file.getBytes();
        	recipeRepository.saveImage(imageId, imageData,email);
        }     
        return imageId;
    }

    public byte[] getImageById(String imageId) {
        return recipeRepository.getImage(imageId);
    }

    public void saveRecipe(Recipe recipe,String email) {
        recipeRepository.save(recipe,email);
    }
    
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
    public List<Recipe> getAllPublishedRecipes() {
        return recipeRepository.findAllPublishedRecipe();
    }

    public List<Recipe> getRecipeById(String email) {
        return recipeRepository.findById(email);
    }
    
    public boolean DeleteRecipe(String recipeId) {
       return recipeRepository.deleteRecipe(recipeId);
    }
    
    public boolean publishRecipe(String recipeId,int flag) {
        return recipeRepository.PublishRecipe(recipeId,flag);
     }
    
    public boolean UpdateRecipe(String recipeId, Recipe recipe) {
    	return recipeRepository.updaterecipe(recipeId, recipe);
    }
    
    public void savePreferences(RecipePreferenceRequest request) {
        recipeRepository.savePreferences(request.getRecipeId(), request.getPreferences());
    }
    
    public boolean savedRecipe(String email,String recipeId) {
    	return recipeRepository.savedrecipe(email,recipeId);
    }
    public boolean DoesSavedItemExist(String email,String id) {
    	return recipeRepository.doesSavedItemExist(email, id);
    }
    public void deletesavedRecipe(String email, String id) {
    	recipeRepository.DeletesavedRecipee(email,id);
    }
    
    public List<Recipe> getSavedRecipeById(String email) {
        return recipeRepository.findSavedRecipesByEmail(email);
    }
    
    public Map<String, Object> getRecipeRating(String recipeId) {
        return recipeRepository.getRecipeRating(recipeId);
    }

    public Double getUserRating(String recipeId, String email) {
        return recipeRepository.getUserRating(recipeId, email);
    }

    public void submitRating(String recipeId, Double rating, String email) {
        recipeRepository.submitRating(recipeId, rating, email);
    }

    public void deleteUserRating(String recipeId, String email) {
        recipeRepository.deleteUserRating(recipeId, email);
    }
    public Map<String, String> getRecipeTitleAndEmail(String recipeId) {
        return recipeRepository.getRecipeDetailsById(recipeId);
    }

}
