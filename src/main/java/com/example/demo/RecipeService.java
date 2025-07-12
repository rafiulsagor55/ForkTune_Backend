package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    public List<Recipe> getRecipeById(String email) {
        return recipeRepository.findById(email);
    }
    
    public boolean DeleteRecipe(String recipeId) {
       return recipeRepository.deleteRecipe(recipeId);
    }
    
    public boolean UpdateRecipe(String recipeId, Recipe recipe) {
    	return recipeRepository.updaterecipe(recipeId, recipe);
    }

}
