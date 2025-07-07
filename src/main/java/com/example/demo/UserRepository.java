package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
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
            user.setProfileImageData(rs.getBytes("profile_image_data"));
            user.setContentType(rs.getString("content_type"));
            return user;
        });
    }
    
    public boolean checkPassword(String email, String password) {
		// Correct the query by removing the extra space after :password
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM users WHERE email = :email AND password = :password";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password);

		// Execute the query correctly using named parameters
		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);
		return count > 0;
	}

}
