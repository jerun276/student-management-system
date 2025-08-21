package com.student_management_system.student.model;

import com.student_management_system.user_management.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String description;

    @Lob
    private String submissionText;

    private LocalDateTime submissionDate;

    private LocalDate dueDate;

    private String grade;

    @Lob
    private String feedback;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    // An assignment is for one specific subject
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // An assignment is assigned to one specific student
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}