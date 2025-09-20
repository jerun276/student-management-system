package com.student_management_system.staff.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime eventDateTime;
    private String location;
    private int maxAttendees;

    @OneToMany(mappedBy = "event")
    private List<EventRegistration> registrations;
}