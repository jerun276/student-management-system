package com.student_management_system.principal.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob // For long text content
    private String content;

    private LocalDateTime publishedDate;

    @ManyToOne
    @JoinColumn(name = "published_by_id")
    private User publishedBy; // The principal who created it
    
    private boolean isActive = true; // For archiving functionality
    
    private boolean isPinned = false; // For pinning important announcements
    
    private String category = "General"; // Category for filtering
    
    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    
    private LocalDateTime expiryDate; // Optional expiry date
    
    private LocalDateTime lastModifiedDate; // Track when last updated
}