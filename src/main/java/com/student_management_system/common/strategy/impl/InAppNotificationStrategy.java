package com.student_management_system.common.strategy.impl;

import com.student_management_system.common.strategy.NotificationStrategy;
import com.student_management_system.common.service.MessageService;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.user_management.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationStrategy implements NotificationStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(InAppNotificationStrategy.class);
    
    private final MessageService messageService;
    private final UserRepository userRepository;
    
    public InAppNotificationStrategy(MessageService messageService, UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }
    
    @Override
    public boolean sendNotification(String recipient, String subject, String message) {
        try {
            // Find user by username or email
            User recipientUser = userRepository.findByUsername(recipient)
                    .or(() -> userRepository.findByEmail(recipient))
                    .orElse(null);
            
            if (recipientUser == null) {
                logger.warn("❌ User not found for recipient: {}", recipient);
                return false;
            }
            
            // Create system user for sending notifications (you might want to create a dedicated system user)
            User systemUser = userRepository.findByUsername("system")
                    .orElse(recipientUser); // Fallback to self if no system user exists
            
            // Send in-app message using existing MessageService
            messageService.sendMessage(systemUser, recipientUser, subject, message);
            
            logger.info("📱 IN-APP NOTIFICATION");
            logger.info("To: {} ({})", recipientUser.getUsername(), recipientUser.getEmail());
            logger.info("Subject: {}", subject);
            logger.info("Message: {}", message);
            logger.info("✅ In-app notification sent successfully");
            
            return true;
        } catch (Exception e) {
            logger.error("❌ Failed to send in-app notification to {}: {}", recipient, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        return messageService != null && userRepository != null;
    }
    
    @Override
    public String getStrategyName() {
        return "In-App";
    }
}
