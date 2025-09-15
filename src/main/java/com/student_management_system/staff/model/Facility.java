package com.student_management_system.staff.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "Main Auditorium", "Sports Ground"
    private String description;
    private int capacity;

    @OneToMany(mappedBy = "facility")
    private List<Booking> bookings;
}