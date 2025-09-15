package com.student_management_system.staff.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    // The user (e.g., a teacher or staff member) who made the booking
    @ManyToOne
    @JoinColumn(name = "booked_by_id")
    private User bookedBy;

    private String purpose; // e.g., "Annual Day Practice", "Soccer Match"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}