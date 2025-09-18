package com.student_management_system.teacher.model;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "grades")
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String gradeValue; // e.g., "A+", "85", "Pass"
    
    @Column(precision = 5, scale = 2)
    private BigDecimal numericGrade; // For calculations (0.00 to 100.00)
    
    @Column(precision = 5, scale = 2)
    private BigDecimal maxMarks = BigDecimal.valueOf(100); // Maximum possible marks
    
    @Lob
    private String comments; // Teacher's feedback
    
    @Column(nullable = false)
    private LocalDateTime gradedDate;
    
    private LocalDateTime lastModifiedDate;
    
    @Enumerated(EnumType.STRING)
    private GradeType gradeType = GradeType.ASSIGNMENT; // ASSIGNMENT, EXAM, QUIZ, PROJECT
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    // Additional fields for grade management
    private String semester; // e.g., "Fall 2024", "Spring 2025"
    
    private String academicYear; // e.g., "2024-2025"
    
    private boolean isPublished = false; // Whether grade is visible to student
    
    private boolean isFinal = false; // Whether this is a final grade
}
