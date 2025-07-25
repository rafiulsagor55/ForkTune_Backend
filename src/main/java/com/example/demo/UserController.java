package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;
import java.util.*;
import java.util.Date;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private UserService userService;
	private final byte[] SECRET = Base64.getEncoder()
			.encode("sQe12Tg7Ld9BxkMfJpRzWuYx9AbVcDeFgHiJkLmNoPqRsTuVwXyZ1234567890ab".getBytes());

	@PostMapping("/send-code")
	public ResponseEntity<?> sendCode(@RequestBody Map<String, String> payload) {
		try {
			String email = payload.get("email");

			if (email != null && !email.isEmpty()) {
				return ResponseEntity.ok(Map.of("message", userService.sendcode(email)));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "Invalid email"));
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	@PostMapping("/verify-email")
	public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload) {
		try {
			String email = payload.get("email");
			String code = payload.get("code");
			Boolean flag = userService.verifyEmail(email, code);
			if (flag == true) {
				long expirationMillis = 7 * 24 * 60 * 60 * 1000L;
				Date now = new Date();
				Date expiryDate = new Date(now.getTime() + expirationMillis);

				String jwt = Jwts.builder().setSubject(email).claim("code", code).setExpiration(expiryDate)
						.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();

				System.out.println(jwt);

				return ResponseEntity.ok(Map.of("code_token", jwt));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "Something went wrong! Please try again."));
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO,
			@RequestHeader("Authorization") String codeToken) {
		try {
			codeToken = codeToken.replace("Bearer ", "");
            System.out.println(userService.getEmailFromToken(codeToken));
			if (userService.checkTokenValidity(codeToken)) {
				userDTO.setEmail(userService.getEmailFromToken(codeToken));
				userService.saveUserDetails(userDTO);
				long expirationMillis = 7 * 24 * 60 * 60 * 1000L;
				Date now = new Date();
				Date expiryDate = new Date(now.getTime() + expirationMillis);
				String jwt = Jwts.builder().setSubject(userDTO.getEmail()).setExpiration(expiryDate)
						.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();
				return ResponseEntity.ok().body(Map.of("message", jwt));
			} else {
				throw new IllegalArgumentException("Something went wrong!");
			}

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Error registering user: " + e.getMessage()));
		}
	}
	
	
	@PostMapping("/login")
	public ResponseEntity<?> Login(@RequestBody Map<String, String> payload) {
		try {
			String email = payload.get("email");
			String password=payload.get("password");
			long expirationMillis = 7 * 24 * 60 * 60 * 1000L;
			Date now = new Date();
			Date expiryDate = new Date(now.getTime() + expirationMillis);
			
			String jwt = Jwts.builder().setSubject(email).setExpiration(expiryDate)
					.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();


			if (email != null && !email.isEmpty() && userService.checkpassword(email, password)) {
				return ResponseEntity.ok(Map.of("token",jwt));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "Invalid email or password"));
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}
	
	@PostMapping("/send-code-for-reset-password")
	public ResponseEntity<?> sendCodeForResetPassword(@RequestBody Map<String, String> payload) {
		try {
			String email = payload.get("email");

			if (email != null && !email.isEmpty()) {
				return ResponseEntity.ok(Map.of("message", userService.sendcodeForResetPassword(email)));
			} else {
				return ResponseEntity.badRequest().body(Map.of("message", "Invalid email"));
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		}
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload,
			@RequestHeader("Authorization") String codeToken) {
		try {
			codeToken = codeToken.replace("Bearer ", "");
			String newPassword = payload.get("newPassword");
            System.out.println(userService.getEmailFromToken(codeToken));
			if (userService.checkTokenValidityAfter(codeToken)) {
				userService.ResetPassword(userService.getEmailFromToken(codeToken),newPassword);
				long expirationMillis = 7 * 24 * 60 * 60 * 1000L;
				Date now = new Date();
				Date expiryDate = new Date(now.getTime() + expirationMillis);
				
				String jwt = Jwts.builder().setSubject(userService.getEmailFromToken(codeToken)).setExpiration(expiryDate)
						.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();

				return ResponseEntity.ok().body(Map.of("message", jwt));
			} else {
				throw new IllegalArgumentException("Something went wrong!");
			}

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Error raseting password: " + e.getMessage()));
		}
	}
	
	@GetMapping("/user/details")
	public ResponseEntity<?> UserDetails(@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
			if(userService.checkTokenValidityAfter(token)) {
				return ResponseEntity.ok(userService.userDetails(userService.getEmailFromToken(token)));
			}
			else {
				throw new IllegalArgumentException("Token is not valid! Please login and try again.");
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
		}
	}
	
	@GetMapping("/user/preferences")
	public ResponseEntity<?> getUserPreferences(@RequestHeader("Authorization") String token) {
	    try {
	        token = token.replace("Bearer ", "");
	        
	        if(userService.checkTokenValidityAfter(token)) {
	            
	            if(userService.UserPreferencesExist(userService.getEmailFromToken(token))) {
	                
	                return ResponseEntity.ok(userService.getPreferences(userService.getEmailFromToken(token)));
	            }

	            return ResponseEntity.ok(new UserPreferences(
	                List.of(),
	                List.of(),
	                List.of(),
	                false
	            ));
	        } else {
	            throw new IllegalArgumentException("Token is not valid! Please login and try again.");
	        }
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
	    }
	}

	
	@PostMapping("/user/update-details")
	public ResponseEntity<?> updateUserDetails(@RequestBody UserDTO userDTO,
			@RequestHeader("Authorization") String token) {
		try {
			System.out.println(userDTO.getName());
			token = token.replace("Bearer ", "");
            System.out.println(userService.getEmailFromToken(token));
			if (userService.checkTokenValidityAfter(token)) {
				userDTO.setEmail(userService.getEmailFromToken(token));
				userService.updateUserDetails(userDTO);
				return ResponseEntity.ok().body(Map.of("message", "Profile updated successfully."));
			} else {
				throw new IllegalArgumentException("Something went wrong!");
			}

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Error updeting user: " + e.getMessage()));
		}
	}
	
	@PostMapping("/user/update-preferences")
	public ResponseEntity<?> saveUserPreferences(@RequestBody UserPreferences preferences,
			@RequestHeader("Authorization") String token) {
		try {
			token = token.replace("Bearer ", "");
            System.out.println(userService.getEmailFromToken(token));
			if (userService.checkTokenValidityAfter(token)) {
				userService.savePreferences(userService.getEmailFromToken(token), preferences);
				return ResponseEntity.ok().body(Map.of("message", "Preferences updated successfully."));
			} else {
				throw new IllegalArgumentException("Something went wrong!");
			}

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Error updeting Preferences: " + e.getMessage()));
		}
	}
	
	@PostMapping("/admin/login")
	public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> payload) {
		try {
			String email = payload.get("email");
			String password=payload.get("password");
			long expirationMillis = 7 * 24 * 60 * 60 * 1000L;
			Date now = new Date();
			Date expiryDate = new Date(now.getTime() + expirationMillis);
			
			String jwt = Jwts.builder().setSubject(email).setExpiration(expiryDate)
					.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();


			if (email != null && !email.isEmpty() && userService.CheckAdminPassword(email, password)) {
				return ResponseEntity.ok(Map.of("token",jwt));
			} else {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
}
