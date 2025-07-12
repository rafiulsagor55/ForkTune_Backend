package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private UserService userService;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                String imageId = recipeService.storeImageInDB(file, userService.getEmailFromToken(token));
                return ResponseEntity.ok(Map.of("imageId", imageId));
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed");
        }
    }

    @PostMapping("/saveRecipes")
    public ResponseEntity<?> saveRecipe(@RequestBody Recipe recipe, @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                recipeService.saveRecipe(recipe, userService.getEmailFromToken(token));
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Recipe saved successfully"));
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to save recipe: " + e.getMessage());
        }
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageId) {
        byte[] imageData = recipeService.getImageById(imageId);
        if (imageData != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/user")
    public ResponseEntity<?> getRecipeById(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                return ResponseEntity.ok(recipeService.getRecipeById(userService.getEmailFromToken(token)));
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteRecipe(@RequestBody Map<String, String>map, @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                boolean isDeleted = recipeService.DeleteRecipe(map.get("id"));
                if (isDeleted) {
                    return ResponseEntity.ok("Recipe deleted successfully");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
                }
            } else {
                throw new IllegalArgumentException("Token is not valid! Please login and try again.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<?> updateRecipe(
        @RequestBody Recipe recipe,
        @RequestHeader("Authorization") String token) {
    	System.out.println("Id: "+recipe.getId());

        try {
        	
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                boolean isUpdated = recipeService.UpdateRecipe(recipe.getId(), recipe);
                if (isUpdated) {
                    return ResponseEntity.ok("Recipe updated successfully");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
                }
            } else {
                throw new IllegalArgumentException("Token is not valid! Please login and try again.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
