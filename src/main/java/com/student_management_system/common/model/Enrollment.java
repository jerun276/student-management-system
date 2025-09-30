package com.student_management_system.common.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Represents the enrollment of a student in a specific classroom for a specific academic year
 * This is the "linking" table that officially places a student in a class
 */
@Entity
@Data
@Table(name = "enrollments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "academic_year_id"}))
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
    
    @Column(nullable = false)
    private LocalDate enrollmentDate;
    
    private LocalDate withdrawalDate; // null if still enrolled
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    private String remarks; // Optional notes about the enrollment
}
