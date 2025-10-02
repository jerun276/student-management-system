package com.student_management_system.teacher.model;

import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import com.student_management_system.common.model.Classroom;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents study materials uploaded by teachers for specific subjects and classrooms
 * Links Subject, Classroom, and Teacher for material management
 */
@Entity
@Data
@Table(name = "study_materials")
public class StudyMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Lob
    private String description;
    
    @Column(nullable = false)
    private String fileName; // The name of the file stored on the server
    
    private String fileType; // File extension/type (pdf, doc, etc.)
    
    private Long fileSize; // File size in bytes

    // Subject this material belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Classroom this material is intended for (optional - can be for all classes of the subject)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    // Teacher who uploaded the material
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();
    
    private boolean isActive = true; // For soft delete
    
    /**
     * Helper method to get material description with context
     */
    public String getFullDescription() {
        String classroomInfo = classroom != null ? " (" + classroom.getFullName() + ")" : " (All Classes)";
        return String.format("%s - %s%s", 
            subject.getFullName(), 
            title,
            classroomInfo);
    }
    
    /**
     * Helper method to get file size in human readable format
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}