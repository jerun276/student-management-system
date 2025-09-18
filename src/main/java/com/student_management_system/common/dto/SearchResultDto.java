package com.student_management_system.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchResultDto {
    
    private Long id;
    private String title;
    private String description;
    private String entityType; // ASSIGNMENT, ANNOUNCEMENT, USER, EVENT, GRADE
    private String url; // Link to the entity
    private LocalDateTime createdDate;
    private Double relevanceScore;
    private String highlightedTitle; // Title with search terms highlighted
    private String highlightedDescription; // Description with search terms highlighted
    private String category; // Additional categorization
    private String author; // Who created this entity
    private String status; // Current status of the entity
    
    // Helper method to get display-friendly entity type
    public String getDisplayEntityType() {
        switch (entityType) {
            case "ASSIGNMENT": return "Assignment";
            case "ANNOUNCEMENT": return "Announcement";
            case "USER": return "User";
            case "EVENT": return "Event";
            case "GRADE": return "Grade";
            default: return entityType;
        }
    }
    
    // Helper method to get icon class for entity type
    public String getEntityIcon() {
        switch (entityType) {
            case "ASSIGNMENT": return "fas fa-tasks";
            case "ANNOUNCEMENT": return "fas fa-bullhorn";
            case "USER": return "fas fa-user";
            case "EVENT": return "fas fa-calendar-alt";
            case "GRADE": return "fas fa-graduation-cap";
            default: return "fas fa-file";
        }
    }
    
    // Helper method to get badge color for entity type
    public String getEntityBadgeColor() {
        switch (entityType) {
            case "ASSIGNMENT": return "badge-primary";
            case "ANNOUNCEMENT": return "badge-info";
            case "USER": return "badge-success";
            case "EVENT": return "badge-warning";
            case "GRADE": return "badge-secondary";
            default: return "badge-ghost";
        }
    }
}
