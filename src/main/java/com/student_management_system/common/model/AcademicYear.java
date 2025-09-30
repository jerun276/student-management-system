package com.student_management_system.common.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Represents an academic year in the school system
 * This is the single source of truth for a school year
 */
@Entity
@Data
@Table(name = "academic_years")
public class AcademicYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name; // e.g., "2024-2025"
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private boolean isActive = false; // Only one academic year should be active at a time
    
    // Note: Reverse relationships will be handled by the respective entities
    // to avoid circular dependencies in imports
}
