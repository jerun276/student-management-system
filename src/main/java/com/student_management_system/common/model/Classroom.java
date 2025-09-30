package com.student_management_system.common.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * Represents a specific class/section in the school (e.g., "Grade 10-A English Medium")
 * This is the core organizational unit that groups students
 */
@Entity
@Data
@Table(name = "classrooms")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // e.g., "10-A", "5-B"
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Medium medium; // ENGLISH, TAMIL, SINHALA
    
    @Column(nullable = false)
    private Integer maxStudents = 40; // Maximum capacity
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_level_id", nullable = false)
    private GradeLevel gradeLevel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private User classTeacher; // The main teacher responsible for this class
    
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Enrollment> enrollments;
    
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Course> courses;
    
    /**
     * Helper method to get the full display name
     * @return Full name like "Grade 10-A (English Medium) - 2024-2025"
     */
    public String getFullName() {
        return String.format("%s-%s (%s Medium) - %s", 
            gradeLevel.getName(), 
            name, 
            medium.toString(), 
            academicYear.getName());
    }
}
