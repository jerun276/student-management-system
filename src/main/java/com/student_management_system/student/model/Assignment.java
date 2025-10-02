package com.student_management_system.student.model;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an assignment given to students
 * Now directly linked to Subject and Classroom for simplified academic structure
 */
@Entity
@Data
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Lob
    private String submissionText;

    private LocalDateTime submissionDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private String grade;

    @Lob
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    // Subject for this assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Classroom for this assignment (to identify which class gets this assignment)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    // An assignment is assigned to one specific student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Teacher who created this assignment (must be assigned to the subject)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    private LocalDateTime createdDate = LocalDateTime.now();
    
    /**
     * Helper method to get assignment description with context
     */
    public String getFullDescription() {
        return String.format("%s - %s (%s)", 
            subject.getFullName(), 
            title, 
            classroom.getFullName());
    }
    
    /**
     * Helper method to check if assignment is overdue
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && 
               (status == AssignmentStatus.ASSIGNED || status == AssignmentStatus.PENDING);
    }
}