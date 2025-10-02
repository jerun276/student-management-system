package com.student_management_system.student.model;

import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.model.TimeSlot;
import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a timetable entry for a subject in a specific classroom
 * Links Subject, Teacher, Classroom, TimeSlot, and day for structured school scheduling
 */
@Entity
@Data
@Table(name = "timetable_entries")
public class TimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Subject being taught in this time slot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Classroom where this subject is taught
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    // Teacher assigned to teach this subject in this time slot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    // NEW: Reference to standardized time slot instead of individual times
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;
    
    // LEGACY: Keep for backward compatibility during transition
    @Column(name = "legacy_start_time")
    private LocalTime startTime;
    
    @Column(name = "legacy_end_time")
    private LocalTime endTime;
    
    private String location; // Optional specific location within classroom
    
    /**
     * Helper method to get the time slot description
     */
    public String getTimeSlotDescription() {
        if (timeSlot != null) {
            return timeSlot.getTimeRange();
        }
        // Fallback to legacy times if timeSlot is null
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime.toString(), endTime.toString());
        }
        return "Time not set";
    }
    
    /**
     * Helper method to get full timetable entry description
     */
    public String getFullDescription() {
        return String.format("%s: %s (%s) - %s", 
            dayOfWeek.toString(), 
            subject.getFullName(), 
            classroom.getFullName(),
            getTimeSlotDescription());
    }
    
    /**
     * Helper method to check if this entry conflicts with another timetable entry
     */
    public boolean conflictsWith(TimetableEntry other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        
        // Check for time overlap using TimeSlot if available
        if (this.timeSlot != null && other.timeSlot != null) {
            return this.timeSlot.conflictsWith(other.timeSlot);
        }
        
        // Fallback to legacy time comparison
        if (this.startTime != null && this.endTime != null && 
            other.startTime != null && other.endTime != null) {
            return !(this.endTime.isBefore(other.startTime) || 
                     this.startTime.isAfter(other.endTime));
        }
        
        return false; // Can't determine conflict without time information
    }
    
    /**
     * Helper method to check if this is during a break period
     */
    public boolean isDuringBreak() {
        return timeSlot != null && timeSlot.isBreakTime();
    }
    
    /**
     * Helper method to check if this is a regular teaching period
     */
    public boolean isTeachingPeriod() {
        return timeSlot != null && timeSlot.isTeachingPeriod();
    }
}