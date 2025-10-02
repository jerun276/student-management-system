package com.student_management_system.common.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalTime;
import java.util.Set;

/**
 * Represents a standardized time slot in the school schedule
 * Examples: "Period 1: 8:00-8:45", "Break: 10:30-10:45", "Lunch: 12:30-13:15"
 */
@Entity
@Data
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Period 1", "Period 2", "Break", "Lunch"

    @Column(nullable = false)
    private LocalTime startTime; // e.g., 08:00

    @Column(nullable = false)
    private LocalTime endTime; // e.g., 08:45

    @Column(nullable = false)
    private Integer orderIndex; // For sorting: 1, 2, 3, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeSlotType type = TimeSlotType.REGULAR; // REGULAR, BREAK, LUNCH, ASSEMBLY

    @Column(nullable = false)
    private Integer durationMinutes; // Calculated field for convenience

    @Column(nullable = false)
    private boolean isActive = true; // For soft delete

    private String description; // Optional description

    // Relationship to timetable entries
    @OneToMany(mappedBy = "timeSlot", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<com.student_management_system.student.model.TimetableEntry> timetableEntries;

    /**
     * Calculate duration in minutes automatically
     */
    @PrePersist
    @PreUpdate
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }

    /**
     * Helper method to get formatted time range
     * @return String like "08:00 - 08:45"
     */
    public String getTimeRange() {
        return String.format("%s - %s", startTime.toString(), endTime.toString());
    }

    /**
     * Helper method to get full display name
     * @return String like "Period 1 (08:00 - 08:45)"
     */
    public String getFullDisplayName() {
        return String.format("%s (%s)", name, getTimeRange());
    }

    /**
     * Check if this time slot conflicts with another
     * @param other Another time slot
     * @return true if there's a time overlap
     */
    public boolean conflictsWith(TimeSlot other) {
        if (other == null) return false;
        
        // No conflict if times don't overlap
        return !(this.endTime.isBefore(other.startTime) || 
                 this.startTime.isAfter(other.endTime));
    }

    /**
     * Check if this time slot is during break time
     * @return true if this is a break or lunch period
     */
    public boolean isBreakTime() {
        return type == TimeSlotType.BREAK || type == TimeSlotType.LUNCH;
    }

    /**
     * Check if this time slot is a regular teaching period
     * @return true if this is a regular teaching period
     */
    public boolean isTeachingPeriod() {
        return type == TimeSlotType.REGULAR;
    }
}
