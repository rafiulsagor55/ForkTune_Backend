package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/recipes")
public class RecipeController {

	@Autowired
	private RecipeService recipeService;
	@Autowired
	private UserService userService;
	@Autowired
	private RecipeNotificationService notificationService;

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

	@GetMapping("/admin")
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
	public ResponseEntity<?> deleteRecipe(@RequestBody Map<String, String> map,
			@RequestHeader("Authorization") String token) {
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
	public ResponseEntity<?> updateRecipe(@RequestBody Recipe recipe, @RequestHeader("Authorization") String token) {
		System.out.println("Id: " + recipe.getId());

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

	@PostMapping("/add-preferences")
	public ResponseEntity<String> addPreferences(@RequestBody RecipePreferenceRequest request,
			@RequestHeader("Authorization") String token) {
		try {

			token = token.replace("Bearer ", "");
			if (userService.checkTokenValidityAfter(token)) {
				recipeService.savePreferences(request);
				return ResponseEntity.ok("Preferences saved successfully.");
			} else {
				throw new IllegalArgumentException("Token is not valid! Please login and try again.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
		}

	}

	@PostMapping("/admin/delete")
	public ResponseEntity<?> deleteRecipeAdmin(@RequestBody Map<String, String> map,
	        @RequestHeader("Authorization") String token) {
	    try {
	        token = token.replace("Bearer ", "");
	        if (userService.checkTokenValidityAdmin(token)) {
	            Map<String, String> recipeTitle = recipeService.getRecipeTitleAndEmail(map.get("id"));
	            boolean isDeleted = recipeService.DeleteRecipe(map.get("id"));
	            if (isDeleted) {
	                String message = "Your recipe '" + recipeTitle.get("title") + "' has been deleted.";
	                notificationService.createNotification(recipeTitle.get("email"), message, map.get("id"));
	                return ResponseEntity.ok("Recipe deleted successfully.");
	            } else {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found.");
	            }
	        } else {
	            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the recipe.");
	    }
	}

	@PostMapping("/admin/publishORunpublish")
	public ResponseEntity<?> PublishorUnpublishRecipeAdmin(@RequestBody Map<String, String> map,
	        @RequestHeader("Authorization") String token) {
	    try {
	        token = token.replace("Bearer ", "");
	        if (userService.checkTokenValidityAdmin(token)) {
	            Map<String, String> recipeTitle = recipeService.getRecipeTitleAndEmail(map.get("id"));
	            boolean isEdited = recipeService.publishRecipe(map.get("id"), Integer.parseInt(map.get("flag")));
	            if (isEdited && Integer.parseInt(map.get("flag")) == 1) {
	                String message = "Your recipe '" + recipeTitle.get("title") + "' has been published and is now visible to users.";
	                notificationService.createNotification(recipeTitle.get("email"), message, map.get("id"));
	                return ResponseEntity.ok("Recipe published successfully.");
	            } else if (isEdited && Integer.parseInt(map.get("flag")) == 0) {
	                String message = "Your recipe '" + recipeTitle.get("title") + "' has been unpublished and is no longer visible to users.";
	                notificationService.createNotification(recipeTitle.get("email"), message, map.get("id"));
	                return ResponseEntity.ok("Recipe unpublished successfully.");
	            } else {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found.");
	            }
	        } else {
	            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the recipe's publish status.");
	    }
	}

	@PostMapping("/toggle-save")
	public ResponseEntity<?> savedRecipe(@RequestBody Map<String, String> map,
			@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
			if (userService.checkTokenValidityAfter(token)) {
				if (!recipeService.DoesSavedItemExist(userService.getEmailFromToken(token), map.get("id"))) {
					boolean isDeleted = recipeService.savedRecipe(userService.getEmailFromToken(token), map.get("id"));
					if (isDeleted) {
						return ResponseEntity.ok("Recipe saved successfully");
					} else {
						return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
					}
				} else {
					recipeService.deletesavedRecipe(userService.getEmailFromToken(token), map.get("id"));
					return ResponseEntity.ok("Removed from saved.");
				}

			} else {
				throw new IllegalArgumentException("Token is not valid! Please login and try again.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
		}
	}

	@GetMapping("/is-saved/{recipeId}")
	public ResponseEntity<?> isRecipeSaved(@PathVariable String recipeId,
			@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
			if (!userService.checkTokenValidityAfter(token)) {
				throw new IllegalArgumentException("Token is not valid! Please login and try again.");
			}
			String email = userService.getEmailFromToken(token);
			boolean isSaved = recipeService.DoesSavedItemExist(email, recipeId);
			return ResponseEntity.ok(Map.of("isSaved", isSaved));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
		}
	}

	@PostMapping("/toggle-save/remove")
	public ResponseEntity<?> RemovesavedRecipe(@RequestBody Map<String, String> map,
			@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
			if (userService.checkTokenValidityAfter(token)) {
				if (!recipeService.DoesSavedItemExist(userService.getEmailFromToken(token), map.get("id"))) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
				} else {
					recipeService.deletesavedRecipe(userService.getEmailFromToken(token), map.get("id"));
					return ResponseEntity.ok("Removed from saved.");
				}

			} else {
				throw new IllegalArgumentException("Token is not valid! Please login and try again.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
		}
	}
	
	@GetMapping("/user/saved")
	public ResponseEntity<?> getSavedRecipeByEmail(@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
			if (userService.checkTokenValidityAfter(token)) {
				return ResponseEntity.ok(recipeService.getSavedRecipeById(userService.getEmailFromToken(token)));
			}
			throw new IllegalArgumentException("Token is not valid! Please login and try again.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
		}
	}
	
	@GetMapping("/user/publish")
	public ResponseEntity<?> getAllPublishedRecipes(@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
			if (userService.checkTokenValidityAfter(token)) {
				return ResponseEntity.ok(recipeService.getAllPublishedRecipes());
			}
			throw new IllegalArgumentException("Token is not valid! Please login and try again.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
		}
	}
	
    @GetMapping("/{recipeId}/rating")
    public ResponseEntity<?> getRecipeRating(@PathVariable String recipeId) {
        try {
            Map<String, Object> ratingData = recipeService.getRecipeRating(recipeId);
            return ResponseEntity.ok(ratingData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load ratings");
        }
    }

    @GetMapping("/{recipeId}/user-rating")
    public ResponseEntity<?> getUserRating(@PathVariable String recipeId,
                                           @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                Double userRating = recipeService.getUserRating(recipeId, userService.getEmailFromToken(token));
                return ResponseEntity.ok(Map.of("rating", userRating));
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
        }
    }

    @PostMapping("/rate")
    public ResponseEntity<?> submitRating(@RequestBody Map<String, Object> ratingRequest,
                                          @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                String recipeId = (String) ratingRequest.get("recipeId");
                Double rating = (Double) ratingRequest.get("rating");
                recipeService.submitRating(recipeId, rating, userService.getEmailFromToken(token));
                return ResponseEntity.ok("Rating submitted successfully!");
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit rating");
        }
    }

    @DeleteMapping("/{recipeId}/rating")
    public ResponseEntity<?> deleteRating(@PathVariable String recipeId,
                                          @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                recipeService.deleteUserRating(recipeId, userService.getEmailFromToken(token));
                return ResponseEntity.ok("Rating removed successfully!");
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove rating");
        }
    }
    
    
    
    @GetMapping("/notification")
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                String email = userService.getEmailFromToken(token);
                System.out.println("Email: "+ email);
                List<RecipeNotification> notifications = notificationService.getNotifications(email);
                return ResponseEntity.ok(notifications);
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load notifications");
        }
    }

    @PostMapping("/notification/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@RequestBody String notificationId,
                                                    @RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                notificationService.markNotificationAsRead(notificationId);
                return ResponseEntity.ok("Notification marked as read");
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark notification as read");
        }
    }

    @PostMapping("/notifications/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                String email = userService.getEmailFromToken(token);
                notificationService.markAllNotificationsAsRead(email);
                return ResponseEntity.ok("All notifications marked as read");
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark all notifications as read");
        }
    }
    
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount(@RequestHeader("Authorization") String token) {
    	try {
            token = token.replace("Bearer ", "");
            if (userService.checkTokenValidityAfter(token)) {
                String email = userService.getEmailFromToken(token);
                int unreadCount = notificationService.getUnreadNotificationCount(email);
                return ResponseEntity.ok(Map.of("count", unreadCount));
            }
            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark all notifications as read");
        }
    }

}
