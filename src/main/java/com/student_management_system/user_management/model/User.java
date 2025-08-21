package com.student_management_system.user_management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    // TODO: Add relationships to Student, Teacher profiles etc.
}