package com.student_management_system.student.model;

import com.student_management_system.common.model.Course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * Represents a general subject in the school curriculum (e.g., "Mathematics", "English", "Science")
 * This is now a simpler, more generic entity. The teacher-student relationships are handled through Course entities.
 */
@Entity
@Data
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name; // e.g., "Mathematics", "English Literature"
    
    private String code; // Optional subject code (e.g., "MATH101", "ENG201")
    
    private String description; // Optional description of the subject
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    // Reverse relationship - a subject can be taught in multiple courses
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Course> courses;
    
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
}