package com.student_management_system.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollStudentDto {
    @NotNull(message = "Student must be selected")
    private Long studentId;

    @NotNull(message = "Classroom must be selected")
    private Long classroomId;

    @NotNull(message = "Academic year must be selected")
    private Long academicYearId;

    @NotNull(message = "Enrollment date is required")
    @PastOrPresent(message = "Enrollment date cannot be in the future")
    private LocalDate enrollmentDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}
