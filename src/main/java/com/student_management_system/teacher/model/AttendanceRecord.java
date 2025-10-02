package com.student_management_system.teacher.model;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Records student attendance for subjects in specific classrooms
 * Links Student, Subject, Classroom, and Teacher for attendance tracking
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

    // Subject for which attendance is being recorded
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Classroom where the attendance was taken
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    // Teacher who recorded the attendance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
    
    private String remarks; // Optional notes about attendance
    
    /**
     * Helper method to get attendance description
     */
    public String getAttendanceDescription() {
        return String.format("%s - %s (%s) on %s: %s", 
            student.getFirstName() + " " + student.getLastName(),
            subject.getFullName(),
            classroom.getFullName(),
            date.toString(),
            status.toString());
    }
    
    /**
     * Helper method to check if attendance is marked as absent
     */
    public boolean isAbsent() {
        return status == AttendanceStatus.ABSENT;
    }
    
    /**
     * Helper method to check if attendance is marked as present
     */
    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT;
    }
}