package com.example.demo;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
	private String email;
    private String name;
    private String password;
    private String gender;
    private LocalDate dob;
    private byte[] profileImageData;
    private String contentType;
    private Timestamp createdAt;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class UserDTO {
	private String email;
    private String name;
    private String password;
    private String gender;
    private LocalDate dob;
    private String profileImage;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class UserPreferences {

	private List<String> dietaryRestrictions;
    private List<String> allergies;
    private List<String> cuisinePreferences;
    private Boolean diabeticFriendly;
}








