package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RecipeNotificationRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<RecipeNotification> findNotificationsByEmail(String email) {
        String sql = "SELECT * FROM recipe_notifications WHERE email = :email ORDER BY id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            RecipeNotification notification = new RecipeNotification();
            notification.setId(rs.getInt("id"));
            notification.setMessage(rs.getString("message"));
            notification.setTime(rs.getTimestamp("time"));
            notification.setUnread(rs.getBoolean("is_unread"));
            notification.setRecipeId(rs.getString("recipe_id"));
            return notification;
        });
    }

    public void updateNotificationStatus(String notificationId, boolean isUnread) {
        String sql = "UPDATE recipe_notifications SET is_unread = :isUnread WHERE id = :notificationId";
        Map<String, Object> params = new HashMap<>();
        params.put("isUnread", isUnread);
        params.put("notificationId", notificationId);

        jdbcTemplate.update(sql, params);
    }

    public void updateAllNotificationsStatus(String email, boolean isUnread) {
        String sql = "UPDATE recipe_notifications SET is_unread = :isUnread WHERE email = :email";
        Map<String, Object> params = new HashMap<>();
        params.put("isUnread", isUnread);
        params.put("email", email);

        jdbcTemplate.update(sql, params);
    }
    
    
    public void insertNotification(String email, String message, String recipeId) {
        String sql = "INSERT INTO recipe_notifications (email, message, recipe_id, is_unread) " +
                     "VALUES (:email, :message, :recipeId, true)";
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("message", message);
        params.put("recipeId", recipeId);
        jdbcTemplate.update(sql, params);
    }
    
//    public void insertNotification(String email, String message, String recipeId) {
//        // SQL query to insert a new notification into the recipe_notifications table
//        String sql = "INSERT INTO recipe_notifications (email, message, recipe_id, is_unread) " +
//                     "VALUES (:email, :message, :recipeId, true)";
//        // Parameters for the query
//        Map<String, Object> params = new HashMap<>();
//        params.put("email", email);
//        params.put("message", message);
//        params.put("recipeId", recipeId);
//
//        // Execute the query
//        jdbcTemplate.update(sql, params);
//    }


    
    public int getUnreadNotificationCount(String email) {
        String sql = "SELECT COUNT(*) FROM recipe_notifications WHERE email = :email AND is_unread = true";
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return jdbcTemplate.queryForObject(sql, params, Integer.class); // Returns the count of unread notifications
    }
    
//    public int getUnreadNotificationCount(String email) {
//        // SQL query to get the count of unread notifications
//        String sql = "SELECT COUNT(*) FROM recipe_notifications WHERE email = :email AND is_unread = true";
//        
//        // Parameters for the query
//        Map<String, Object> params = new HashMap<>();
//        params.put("email", email);
//        
//        // Execute the query and return the count of unread notifications
//        return jdbcTemplate.queryForObject(sql, params, Integer.class); 
//    }


}
