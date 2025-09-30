package com.student_management_system.common.model;

import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * Represents a specific subject being taught to a specific classroom by a specific teacher
 * This replaces the direct Subject-Teacher-Student relationship with a more structured approach
 * Example: "Mathematics for Grade 10-A taught by Mr. Smith in 2024-2025"
 */
@Entity
@Data
@Table(name = "courses",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "classroom_id", "academic_year_id"}))
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject; // The general subject (e.g., "Mathematics")
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom; // The specific class (e.g., "10-A")
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher; // The teacher for this specific course
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    private String description; // Optional course description
    
    // Reverse relationships for assignments, attendance, etc.
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.student.model.Assignment> assignments;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.teacher.model.AttendanceRecord> attendanceRecords;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.student.model.TimetableEntry> timetableEntries;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.teacher.model.StudyMaterial> studyMaterials;
    
    /**
     * Helper method to get the full course name
     * @return Full name like "Mathematics - Grade 10-A (English Medium) - 2024-2025"
     */
    public String getFullName() {
        return String.format("%s - %s", 
            subject.getName(), 
            classroom.getFullName());
    }
}
