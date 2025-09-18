package com.student_management_system.principal.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementDto {
    
    private Long id;
    
    @NotBlank(message = "Announcement title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Announcement content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;
    
    private LocalDateTime publishedDate;
    
    private String publishedByName; // For display purposes
    
    private boolean isActive = true;
    
    private boolean isPinned = false;
    
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    private LocalDateTime expiryDate;
    
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
}
