package com.student_management_system.admin.dto;

import com.student_management_system.user_management.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserManagementDto {
    
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    
    private String confirmPassword;
    
    @NotNull(message = "Role is required")
    private Role role;
    
    private boolean enabled = true;
    
    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    // For parent-child relationships
    private Long parentId;
    
    private String parentName; // For display purposes
}
