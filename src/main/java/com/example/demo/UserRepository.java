package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class UserRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplatePure;
    
    public boolean doesEmailExist(String email) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM users WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);

		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

		return count > 0;
	}
    
    public void deleteByEmail(String email) {
		String DELETE_BY_EMAIL_SQL = "DELETE FROM codes WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		jdbcTemplate.update(DELETE_BY_EMAIL_SQL, params);
	}
    
    public void insertCode(String email, String code) {
		String INSERT_CODE_SQL = "INSERT INTO codes (email, code) " + "VALUES (:email, :code)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("code", code);

		jdbcTemplate.update(INSERT_CODE_SQL, params);
	}
    
    
    public boolean doesCodeExistForEmail(String email, String code) {
		String CHECK_EMAIL_AND_CODE_SQL = "SELECT COUNT(*) FROM codes WHERE email = :email AND code = :code";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("code", code);

		Integer count = jdbcTemplate.queryForObject(CHECK_EMAIL_AND_CODE_SQL, params, Integer.class);

		return count != null && count > 0;
	}
    
    public void incrementCount(String email) {
		String INCREMENT_COUNT_SQL = "UPDATE codes SET count = count + 1 WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		jdbcTemplate.update(INCREMENT_COUNT_SQL, params);
	}

	public int getCount(String email) {
		String GET_COUNT_SQL = "SELECT count FROM codes WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		try {
			Integer count = jdbcTemplate.queryForObject(GET_COUNT_SQL, params, Integer.class);
			return count != null ? count : 0;
		} catch (Exception e) {
			return -1;
		}
	}
	
	public int insertUserDetails(User user) {
        String sql = "INSERT INTO users (email, name, password, gender, dob, profile_image, content_type) " +
                     "VALUES (:email, :name, :password, :gender, :dob, :profile_image_data, :content_type)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", user.getEmail());
        params.addValue("name", user.getName());
        params.addValue("password", user.getPassword());
        params.addValue("gender", user.getGender());
        params.addValue("dob", user.getDob());
        params.addValue("profile_image_data", user.getProfileImageData()); 
        params.addValue("content_type", user.getContentType());

        return jdbcTemplate.update(sql, params);
    }

    // Example of a method to find a user by email
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email);

        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
            User user = new User();
            user.setEmail(rs.getString("email"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
            user.setGender(rs.getString("gender"));
            user.setDob(rs.getDate("dob").toLocalDate());
            user.setProfileImageData(rs.getBytes("profile_image"));
            user.setContentType(rs.getString("content_type"));
            return user;
        });
    }
    
    public boolean checkPassword(String email, String password) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM users WHERE email = :email AND password = :password";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password);

		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);
		return count > 0;
	}
    
    public void setNewPassword(String email,String password) {
		String INCREMENT_COUNT_SQL = "UPDATE users SET password =:password WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password); 
		jdbcTemplate.update(INCREMENT_COUNT_SQL, params);
	}
    
    public void saveUserPreferences(String email, UserPreferences preferences) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonPrefs = mapper.writeValueAsString(preferences);

            String sql = "INSERT INTO user_preferences (email, preferences_json) " +
                    "VALUES (:email, :preferencesJson) " +
                    "ON DUPLICATE KEY UPDATE preferences_json = :preferencesJson";

            Map<String, Object> params = new HashMap<>();
            params.put("email", email);
            params.put("preferencesJson", jsonPrefs);

            jdbcTemplate.update(sql, params); 
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving preferences: " + e.getMessage());
        }

    }
    
//    public void saveUserPreferences(String email, UserPreferences preferences) {
//    try {
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonPrefs = mapper.writeValueAsString(preferences);
//
//        String sql =
//    "INSERT INTO user_preferences (email, preferences_json) VALUES (?, ?::jsonb) " +
//    "ON CONFLICT (email) DO UPDATE SET preferences_json = EXCLUDED.preferences_json";
//
//
//        jdbcTemplatePure.update(sql, email, jsonPrefs);
//
//    } catch (Exception e) {
//        throw new IllegalArgumentException("Error saving preferences: " + e.getMessage(), e);
//    }
//}


        public UserPreferences getUserPreferences(String email) {
            try {
                String sql = "SELECT preferences_json FROM user_preferences WHERE email = :email";
                
                Map<String, Object> params = new HashMap<>();
                params.put("email", email);

                String jsonPrefs = jdbcTemplate.queryForObject(sql, params, String.class);

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonPrefs, UserPreferences.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error retrieving preferences: " + e.getMessage());
            }
        }
    
        public boolean UserPreferencesExist(String email) {
            String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM user_preferences WHERE email = :email";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("email", email);

            int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

            return count > 0;
        }

    
    public int updateUserDetails(User user) {
        String sql = "UPDATE users SET name = :name, gender = :gender, dob = :dob, profile_image = :profile_image, content_type = :content_type " +
                     "WHERE email = :email";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", user.getName());
        params.addValue("gender", user.getGender());
        params.addValue("dob", user.getDob());
        params.addValue("profile_image", user.getProfileImageData());
        params.addValue("email", user.getEmail());
        params.addValue("content_type", user.getContentType());

        return jdbcTemplate.update(sql, params);
    }
    

    public boolean checkAdminPassword(String email, String password) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM admin WHERE email = :email AND password = :password";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password);

		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);
		return count > 0;
	}

}
