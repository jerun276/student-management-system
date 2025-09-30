package com.student_management_system.teacher.model;

import com.student_management_system.common.model.Course;
import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Records student attendance for courses
 * Now linked to Course for better academic structure while maintaining backward compatibility
 */
@Entity
@Data
@Table(name = "attendance_records")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // NEW: Link to Course instead of Subject directly
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // LEGACY: Keep subject link for backward compatibility
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
    
    private String remarks; // Optional notes about attendance
    
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