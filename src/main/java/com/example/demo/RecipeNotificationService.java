package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeNotificationService {

    @Autowired
    private RecipeNotificationRepository notificationRepository;

    public List<RecipeNotification> getNotifications(String email) {
        return notificationRepository.findNotificationsByEmail(email);
    }

    public void markNotificationAsRead(String notificationId) {
        notificationRepository.updateNotificationStatus(notificationId, false);
    }

    public void markAllNotificationsAsRead(String email) {
        notificationRepository.updateAllNotificationsStatus(email, false);
    }
    
    public void createNotification(String email, String message, String recipeId) {
        notificationRepository.insertNotification(email, message, recipeId);
    }
    
    public int getUnreadNotificationCount(String email) {
        return notificationRepository.getUnreadNotificationCount(email);
    }
}
