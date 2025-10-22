package com.student_management_system.common.demo;

import com.student_management_system.common.service.NotificationService;
import com.student_management_system.common.strategy.impl.EmailNotificationStrategy;
import com.student_management_system.common.strategy.impl.InAppNotificationStrategy;
import com.student_management_system.common.strategy.impl.SMSNotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationStrategyDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationStrategyDemo.class);
    
    private final NotificationService notificationService;
    private final EmailNotificationStrategy emailStrategy;
    private final SMSNotificationStrategy smsStrategy;
    private final InAppNotificationStrategy inAppStrategy;
    
    public NotificationStrategyDemo(NotificationService notificationService,
                                  EmailNotificationStrategy emailStrategy,
                                  SMSNotificationStrategy smsStrategy,
                                  InAppNotificationStrategy inAppStrategy) {
        this.notificationService = notificationService;
        this.emailStrategy = emailStrategy;
        this.smsStrategy = smsStrategy;
        this.inAppStrategy = inAppStrategy;
    }
    
    public void demonstrateStrategyPattern() {
        logger.info("🎯 STRATEGY PATTERN DEMONSTRATION");
        logger.info("=================================");
        
        String recipient = "student@example.com";
        String subject = "Assignment Due Reminder";
        String message = "Your assignment for Mathematics is due tomorrow at 11:59 PM.";
        
        // Strategy 1: Email Notification
        logger.info("\n📧 Using Email Strategy:");
        notificationService.setStrategy(emailStrategy);
        notificationService.sendNotification(recipient, subject, message);
        
        // Strategy 2: SMS Notification
        logger.info("\n📱 Using SMS Strategy:");
        notificationService.setStrategy(smsStrategy);
        notificationService.sendNotification(recipient, subject, message);
        
        // Strategy 3: In-App Notification
        logger.info("\n💬 Using In-App Strategy:");
        notificationService.setStrategy(inAppStrategy);
        notificationService.sendNotification(recipient, subject, message);
        
        // Strategy 4: Broadcast (Multiple Strategies)
        logger.info("\n📢 Broadcasting via Multiple Strategies:");
        notificationService.broadcastNotification(recipient, subject, message, 
                                                emailStrategy, inAppStrategy);
        
        // Show current strategy
        logger.info("\n🔍 Current Strategy: {}", notificationService.getCurrentStrategyName());
        
        // Show available strategies
        logger.info("📋 Available Strategies: {}", 
                   notificationService.getAvailableStrategies()
                           .stream()
                           .map(strategy -> strategy.getStrategyName())
                           .toList());
        
        logger.info("\n✅ Strategy Pattern demonstration completed!");
    }
    
    public void demonstrateContextSwitching() {
        logger.info("\n🔄 CONTEXT SWITCHING DEMONSTRATION");
        logger.info("==================================");
        
        String recipient = "teacher@example.com";
        String subject = "Grade Update";
        String message = "Student grades have been updated for your class.";
        
        // Simulate different scenarios requiring different notification strategies
        
        // Scenario 1: Urgent notification - use SMS
        logger.info("\n🚨 URGENT: Using SMS for immediate delivery");
        notificationService.setStrategy(smsStrategy);
        notificationService.sendNotification(recipient, "URGENT: " + subject, message);
        
        // Scenario 2: Formal notification - use Email
        logger.info("\n📄 FORMAL: Using Email for official communication");
        notificationService.setStrategy(emailStrategy);
        notificationService.sendNotification(recipient, subject, message);
        
        // Scenario 3: Internal notification - use In-App
        logger.info("\n🏠 INTERNAL: Using In-App for internal communication");
        notificationService.setStrategy(inAppStrategy);
        notificationService.sendNotification(recipient, subject, message);
        
        logger.info("\n✅ Context switching demonstration completed!");
    }
}
