package com.example.demo;

import java.util.Base64;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class UserService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserRepository userRepository;

	private final byte[] SECRET = Base64.getEncoder()
			.encode("sQe12Tg7Ld9BxkMfJpRzWuYx9AbVcDeFgHiJkLmNoPqRsTuVwXyZ1234567890ab".getBytes());

	public String generateVerificationCode() {
		return String.format("%06d", new Random().nextInt(999999)); // Generate a 6-digit code
	}

	public void sendVerificationCode(String email) {
		String code = generateVerificationCode();
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Your Verification Code for Online Shop.");
		message.setText("Your code is: " + code);
		mailSender.send(message);
		userRepository.insertCode(email, code);
	}

	public String sendcode(String email) {
		if (!userRepository.doesEmailExist(email)) {
			userRepository.deleteByEmail(email);
			sendVerificationCode(email);
			return "Verification code sent to: " + email;
		} else {
			throw new IllegalArgumentException("You already have an account! Please Sign in.");
		}
	}

	public Boolean verifyEmail(String email, String code) {
		int count = userRepository.getCount(email);
		if (count == -1) {
			throw new IllegalArgumentException("Email does not exist!");
		} else if (count >= 5) {
			throw new IllegalArgumentException("Too many incorrect attempts! please resend your code and try again.");
		} else if (!userRepository.doesCodeExistForEmail(email, code)) {
			userRepository.incrementCount(email);
			throw new IllegalArgumentException("Invalid verification code! Please try again.");
		} else {
//    		userRepository.deleteByEmail(email);
			return true;
		}

	}

	public String tokenBuilder(String email) {
		long expirationMillis = 7 * 24 * 60 * 60 * 1000L;
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationMillis);
		return Jwts.builder().setSubject(email).setExpiration(expiryDate)
				.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();
	}

//	public Boolean checkpassword(String email, String password) {
//		Boolean flag=userRepository.checkPassword(email, password);
//		if(flag)return true;
//		else throw new IllegalArgumentException("Invalid email or password");
//	}

	public String getEmailFromToken(String jwt) {
		if (jwt == null)
			return null;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();
			return claims.getSubject();
		} catch (Exception e) {
			return null;
		}
	}

	public Boolean checkTokenValidity(String jwt) {
		if (jwt == null)
			return false;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();
			if (!userRepository.doesEmailExist(claims.getSubject())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	public void saveUserDetails(UserDTO userDTO) {
		try {
			if (!userRepository.doesEmailExist(userDTO.getEmail())) {
				byte[] decodedBytes = null;
		        String contentType = null;
		        
		        if (userDTO.getProfileImage() != null && userDTO.getProfileImage().startsWith("data:")) {
		            int commaIndex = userDTO.getProfileImage().indexOf(",");
		            if (commaIndex != -1) {
		                contentType = userDTO.getProfileImage().substring(5, commaIndex); // Extract MIME type
		                String imageData = userDTO.getProfileImage().substring(commaIndex + 1); // Remove the base64 prefix
		                decodedBytes = Base64.getDecoder().decode(imageData); // Decode base64
		            }
		        }
		        
		        // Validate that we have valid data
		        if (!contentType.startsWith("image")) {
		            throw new IllegalArgumentException("Invalid image data.");
		        }

				userRepository.insertUserDetails(User.builder().email(userDTO.getEmail()).name(userDTO.getName())
						.password(userDTO.getPassword())
						.gender(userDTO.getGender())
						.dob(userDTO.getDob())
						.profileImageData(decodedBytes)
						.contentType(contentType).build()
						);
				userRepository.deleteByEmail(userDTO.getEmail());
			} else {
				throw new IllegalArgumentException("Something went wrong!");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	public Boolean checkpassword(String email, String password) {
		Boolean flag=userRepository.checkPassword(email, password);
		if(flag)return true;
		else throw new IllegalArgumentException("Invalid email or password");
	}

}
