package com.student_management_system.common.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class BudgetRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}