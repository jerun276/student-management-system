package com.student_management_system.teacher.dto;

import com.student_management_system.teacher.model.GradeType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeDto {
    
    private Long id;
    
    @NotBlank(message = "Grade value is required")
    @Size(max = 10, message = "Grade value must not exceed 10 characters")
    private String gradeValue;
    
    @DecimalMin(value = "0.0", message = "Numeric grade must be non-negative")
    @DecimalMax(value = "100.0", message = "Numeric grade cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Invalid numeric grade format")
    private BigDecimal numericGrade;
    
    @DecimalMin(value = "1.0", message = "Maximum marks must be positive")
    @DecimalMax(value = "1000.0", message = "Maximum marks cannot exceed 1000")
    private BigDecimal maxMarks = BigDecimal.valueOf(100);
    
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;
    
    @NotNull(message = "Grade type is required")
    private GradeType gradeType = GradeType.ASSIGNMENT;
    
    @NotNull(message = "Subject is required")
    private Long subjectId;
    
    private String subjectName; // For display
    
    @NotNull(message = "Student is required")
    private Long studentId;
    
    private String studentName; // For display
    
    private Long assignmentId; // Optional - for assignment-based grades
    
    private String assignmentTitle; // For display
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;
    
    @NotBlank(message = "Academic year is required")
    @Size(max = 10, message = "Academic year must not exceed 10 characters")
    private String academicYear;
    
    private boolean isPublished = false;
    
    private boolean isFinal = false;
    
    private LocalDateTime gradedDate;
    
    private String teacherName; // For display
}
