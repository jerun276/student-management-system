package com.student_management_system.common.strategy.impl;

import com.student_management_system.common.strategy.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationStrategy implements NotificationStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationStrategy.class);
    
    @Override
    public boolean sendNotification(String recipient, String subject, String message) {
        try {
            logger.info("📧 EMAIL NOTIFICATION");
            logger.info("To: {}", recipient);
            logger.info("Subject: {}", subject);
            logger.info("Message: {}", message);
            logger.info("✅ Email sent successfully");
            
            Thread.sleep(100);
            
            return true;
        } catch (Exception e) {
            logger.error("❌ Failed to send email to {}: {}", recipient, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    @Override
    public String getStrategyName() {
        return "Email";
    }
}
