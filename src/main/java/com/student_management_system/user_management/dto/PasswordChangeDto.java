package com.student_management_system.user_management.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDto {

    @NotEmpty(message = "Current password cannot be empty")
    private String currentPassword;

    @NotEmpty(message = "New password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    @NotEmpty(message = "Please confirm your new password")
    private String confirmNewPassword;
}
