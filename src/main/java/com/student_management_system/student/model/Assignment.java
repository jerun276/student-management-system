package com.student_management_system.student.model;

import com.student_management_system.common.model.Course;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an assignment given to students
 * Now linked to Course instead of Subject directly for better academic structure
 */
@Entity
@Data
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Lob
    private String submissionText;

    private LocalDateTime submissionDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private String grade;

    @Lob
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    // NEW: Link to Course instead of Subject directly
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // LEGACY: Keep subject link for backward compatibility during transition
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // An assignment is assigned to one specific student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Teacher can be inferred from Course, but keeping for direct access
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    private LocalDateTime createdDate = LocalDateTime.now();
    
    /**
     * Helper method to get the teacher from course if not directly set
     */
    public User getEffectiveTeacher() {
        if (teacher != null) {
            return teacher;
        }
        if (course != null) {
            return course.getTeacher();
        }
        return null;
    }
    
    /**
     * Helper method to get the subject from course if not directly set
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
}