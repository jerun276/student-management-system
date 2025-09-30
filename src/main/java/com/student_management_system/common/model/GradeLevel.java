package com.student_management_system.common.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a grade or year level in the school (e.g., "Grade 1", "Grade 10")
 */
@Entity
@Data
@Table(name = "grade_levels")
public class GradeLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name; // e.g., "Grade 1", "Grade 10", "A/L Commerce"
    
    @Column(nullable = false)
    private Integer level; // Numeric level for ordering (1, 2, 3... 13)
    
    private String description; // Optional description
    
    // Note: Reverse relationship to classrooms will be handled by Classroom entity
}
