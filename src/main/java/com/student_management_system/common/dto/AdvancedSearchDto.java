package com.student_management_system.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Data
public class AdvancedSearchDto {
    
    @NotBlank(message = "Search query is required")
    @Size(min = 2, max = 255, message = "Search query must be between 2 and 255 characters")
    private String query;
    
    private List<String> entityTypes = new ArrayList<>(); // ASSIGNMENT, ANNOUNCEMENT, USER, EVENT, GRADE
    
    private LocalDate dateFrom;
    
    private LocalDate dateTo;
    
    private String category; // For filtering by category
    
    private String status; // For filtering by status
    
    private String author; // For filtering by author/creator
    
    private String sortBy = "relevance"; // relevance, date, title
    
    private String sortDirection = "desc"; // asc, desc
    
    private boolean includeArchived = false;
    
    private boolean exactMatch = false; // For exact phrase matching
    
    private List<String> tags = new ArrayList<>(); // For tag-based filtering
    
    private String role; // For filtering by user role
    
    private String subject; // For filtering by subject
    
    private String semester; // For filtering by semester
    
    private String academicYear; // For filtering by academic year
    
    // Helper methods
    public boolean hasEntityTypeFilter() {
        return entityTypes != null && !entityTypes.isEmpty();
    }
    
    public boolean hasDateFilter() {
        return dateFrom != null || dateTo != null;
    }
    
    public boolean hasCategoryFilter() {
        return category != null && !category.trim().isEmpty();
    }
    
    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }
    
    public boolean hasAuthorFilter() {
        return author != null && !author.trim().isEmpty();
    }
    
    public boolean hasTagFilter() {
        return tags != null && !tags.isEmpty();
    }
    
    // Method to add entity type
    public void addEntityType(String entityType) {
        if (entityTypes == null) {
            entityTypes = new ArrayList<>();
        }
        if (!entityTypes.contains(entityType)) {
            entityTypes.add(entityType);
        }
    }
    
    // Method to remove entity type
    public void removeEntityType(String entityType) {
        if (entityTypes != null) {
            entityTypes.remove(entityType);
        }
    }
    
    // Method to add tag
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    // Method to remove tag
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
}
