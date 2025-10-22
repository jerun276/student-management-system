package com.student_management_system.common.service;

import com.student_management_system.common.strategy.NotificationStrategy;
import com.student_management_system.common.strategy.impl.EmailNotificationStrategy;
import com.student_management_system.common.strategy.impl.InAppNotificationStrategy;
import com.student_management_system.common.strategy.impl.SMSNotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final EmailNotificationStrategy emailStrategy;
    private final SMSNotificationStrategy smsStrategy;
    private final InAppNotificationStrategy inAppStrategy;
    
    private NotificationStrategy currentStrategy;
    
    public NotificationService(EmailNotificationStrategy emailStrategy, 
                             SMSNotificationStrategy smsStrategy,
                             InAppNotificationStrategy inAppStrategy) {
        this.emailStrategy = emailStrategy;
        this.smsStrategy = smsStrategy;
        this.inAppStrategy = inAppStrategy;
        // Default to in-app notifications
        this.currentStrategy = inAppStrategy;
    }
    
    /**
     * Set the notification strategy to use
     */
    public void setStrategy(NotificationStrategy strategy) {
        if (strategy != null && strategy.isAvailable()) {
            this.currentStrategy = strategy;
            logger.debug("Notification strategy set to: {}", strategy.getStrategyName());
        } else {
            logger.warn("Strategy is null or not available, keeping current strategy: {}", 
                       currentStrategy.getStrategyName());
        }
    }
    
    /**
     * Send notification using the current strategy
     */
    public boolean sendNotification(String recipient, String subject, String message) {
        if (currentStrategy == null) {
            logger.error("No notification strategy is set");
            return false;
        }
        
        logger.info("Sending notification via {}", currentStrategy.getStrategyName());
        return currentStrategy.sendNotification(recipient, subject, message);
    }
    
    /**
     * Send notification using email strategy
     */
    public boolean sendEmailNotification(String recipient, String subject, String message) {
        setStrategy(emailStrategy);
        return sendNotification(recipient, subject, message);
    }
    
    /**
     * Send notification using SMS strategy
     */
    public boolean sendSMSNotification(String recipient, String subject, String message) {
        setStrategy(smsStrategy);
        return sendNotification(recipient, subject, message);
    }
    
    /**
     * Send notification using in-app strategy
     */
    public boolean sendInAppNotification(String recipient, String subject, String message) {
        setStrategy(inAppStrategy);
        return sendNotification(recipient, subject, message);
    }
    
    /**
     * Send notification via multiple strategies (broadcast)
     */
    public void broadcastNotification(String recipient, String subject, String message, 
                                    NotificationStrategy... strategies) {
        List<NotificationStrategy> strategyList = strategies.length > 0 ? 
            Arrays.asList(strategies) : 
            Arrays.asList(emailStrategy, inAppStrategy); // Default strategies
        
        logger.info("Broadcasting notification to {} via {} strategies", recipient, strategyList.size());
        
        for (NotificationStrategy strategy : strategyList) {
            if (strategy.isAvailable()) {
                try {
                    strategy.sendNotification(recipient, subject, message);
                } catch (Exception e) {
                    logger.error("Failed to send notification via {}: {}", 
                               strategy.getStrategyName(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get all available strategies
     */
    public List<NotificationStrategy> getAvailableStrategies() {
        return Arrays.asList(emailStrategy, smsStrategy, inAppStrategy)
                .stream()
                .filter(NotificationStrategy::isAvailable)
                .toList();
    }
    
    /**
     * Get current strategy name
     */
    public String getCurrentStrategyName() {
        return currentStrategy != null ? currentStrategy.getStrategyName() : "None";
    }
}
