package com.student_management_system.student.model;

import com.student_management_system.common.model.GradeLevel;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a subject in the school curriculum for a specific grade level
 * Each subject is tied to a grade level and can have multiple teachers
 * Example: "Mathematics for Grade 10", "English for Grade 5"
 */
@Entity
@Data
@Table(name = "subjects", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"name", "grade_level_id"}))
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // e.g., "Mathematics", "English Literature"
    
    @Column(nullable = false, unique = true)
    private String subjectCode; // e.g., "MATH10", "ENG05", "SCI08"
    
    private String description; // Optional description of the subject
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    // NEW: Grade level relationship - each subject belongs to a specific grade
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_level_id", nullable = false)
    private GradeLevel gradeLevel;
    
    // NEW: Many-to-many relationship with teachers
    @ManyToMany(mappedBy = "subjects")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> teachers = new HashSet<>();
    
    // Keep backward compatibility for existing assignments
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Assignment> assignments;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<TimetableEntry> timetableEntries;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.teacher.model.AttendanceRecord> attendanceRecords;
    
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.teacher.model.StudyMaterial> studyMaterials;
    
    /**
     * Helper method to get the full subject name with grade
     * @return Full name like "Mathematics - Grade 10"
     */
    public String getFullName() {
        return String.format("%s - %s", name, gradeLevel.getName());
    }
    
    /**
     * Helper method to check if a teacher is assigned to this subject
     * @param teacher The teacher to check
     * @return true if the teacher is assigned to this subject
     */
    public boolean hasTeacher(User teacher) {
        return teachers.contains(teacher);
    }
}