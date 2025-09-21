package com.student_management_system.user_management.dto;

import com.student_management_system.user_management.model.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileDto {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private Role role;
}
