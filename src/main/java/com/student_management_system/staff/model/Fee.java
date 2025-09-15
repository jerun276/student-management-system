package com.student_management_system.staff.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // e.g., "Annual Tuition Fee 2025"

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private FeeStatus status;
    private LocalDate lastReminderSent;


    @OneToMany(mappedBy = "fee", cascade = CascadeType.ALL)
    private List<Payment> payments;
}