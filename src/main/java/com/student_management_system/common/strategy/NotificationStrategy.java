package com.student_management_system.common.strategy;

// Strategy interface for different notification methods
public interface NotificationStrategy {

    boolean sendNotification(String recipient, String subject, String message);
    
    boolean isAvailable();

    String getStrategyName();
}
