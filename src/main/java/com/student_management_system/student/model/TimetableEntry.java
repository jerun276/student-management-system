package com.student_management_system.student.model;

import com.student_management_system.common.model.Course;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a timetable entry for a course
 * Now linked to Course instead of Subject directly for better academic structure
 */
@Entity
@Data
@Table(name = "timetable_entries")
public class TimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NEW: Link to Course instead of Subject directly
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // LEGACY: Keep subject link for backward compatibility
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // This can now be inferred from Course->Classroom->Enrollments, but keeping for direct access
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    private String location; // Classroom/room location
    
    /**
     * Helper method to get the effective subject
     */
    public Subject getEffectiveSubject() {
        if (subject != null) {
            return subject;
        }
        if (course != null) {
            return course.getSubject();
        }
        return null;
    }
    
    /**
     * Helper method to get the teacher from course
     */
    public User getTeacher() {
        if (course != null) {
            return course.getTeacher();
        }
        return null;
    }
}