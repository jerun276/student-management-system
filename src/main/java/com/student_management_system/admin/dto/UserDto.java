package com.student_management_system.admin.dto;

import com.student_management_system.user_management.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;

    @NotEmpty(message = "Username cannot be empty")
    private String username;

    @NotEmpty(message = "Email cannot be empty")
    @Email
    private String email;

    @NotEmpty(message = "First name cannot be empty")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;

    private LocalDate dateOfBirth;

    private String address;

    private String phoneNumber;

    @Pattern(regexp = "^([0-9]{9}[vV]|[0-9]{12})$", message = "NIC format is invalid")
    private String nic;

    private Role role;

    private boolean enabled;
}