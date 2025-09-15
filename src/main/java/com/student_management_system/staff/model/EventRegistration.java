package com.student_management_system.staff.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    // The student who is registered
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    private LocalDateTime registrationDate;
}