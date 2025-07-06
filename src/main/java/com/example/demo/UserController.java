package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

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

			// Check if email is valid and return the response
			if (email != null && !email.isEmpty()) {
				// Assuming the service sends a code to the email
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
			// Remove "Bearer " from Authorization header to extract the token
			codeToken = codeToken.replace("Bearer ", "");
            System.out.println(userService.getEmailFromToken(codeToken));
			if (userService.checkTokenValidity(codeToken)) {
				userDTO.setEmail(userService.getEmailFromToken(codeToken));
				userService.saveUserDetails(userDTO);
				return ResponseEntity.ok().body(Map.of("message", "User registered successfully"));
			} else {
				throw new IllegalArgumentException("Something went wrong!");
			}

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("message", "Error registering user: " + e.getMessage()));
		}
	}

//	@PostMapping("/signup")
//	public ResponseEntity<?> signUp(@RequestBody Map<String, String> payload) {
//		if (payload.get("email") == null || payload.get("password") == null) {
//			return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
//		}
//
//		if (userRepository.existsByEmail(payload.get("email"))) {
//			return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
//		}
//
//		try {
//			// Process the user creation
//			User user = new User(payload.get("name"), payload.get("email"), payload.get("password"),
//					payload.get("gender"), LocalDate.parse(payload.get("dob"), DateTimeFormatter.ISO_DATE));
//
//			userRepository.save(user);
//
//			// Ensure that a valid response is sent
//			return ResponseEntity.ok(Map.of("message", "User registered successfully", "userId", user.getId().toString() // Send
//																															// user
//																															// ID
//																															// if
//																															// needed
//			));
//
//		} catch (Exception e) {
//			return ResponseEntity.internalServerError().body(Map.of("error", "Error registering user"));
//		}
//	}

}
