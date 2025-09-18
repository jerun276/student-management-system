package com.student_management_system.teacher.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignmentDto {
    
    private Long id;
    
    @NotBlank(message = "Assignment title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Assignment description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
    
    @NotNull(message = "Subject is required")
    private Long subjectId;
    
    private String subjectName; // For display purposes
    
    @Min(value = 0, message = "Maximum marks must be positive")
    @Max(value = 1000, message = "Maximum marks cannot exceed 1000")
    private Integer maxMarks = 100;
    
    private String instructions;
    
    private boolean allowLateSubmission = false;
    
    private Integer latePenaltyPercent = 0;
}
