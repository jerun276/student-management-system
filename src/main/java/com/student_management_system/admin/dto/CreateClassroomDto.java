package com.student_management_system.admin.dto;

import com.student_management_system.common.model.Medium;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateClassroomDto {
    @NotEmpty(message = "Classroom name cannot be empty")
    @Size(min = 1, max = 10, message = "Classroom name must be between 1 and 10 characters")
    @Pattern(regexp = "^[A-Z][a-zA-Z0-9]*$", message = "Classroom name must start with a capital letter and contain only letters and numbers")
    private String name;

    @NotNull(message = "Grade level must be selected")
    @Min(value = 1, message = "Grade level must be at least 1")
    @Max(value = 13, message = "Grade level cannot exceed 13")
    private Long gradeLevelId;

    @NotNull(message = "Academic year must be selected")
    private Long academicYearId;

    @NotNull(message = "Medium of instruction must be selected")
    private Medium medium;

    @NotNull(message = "Maximum students must be specified")
    @Min(value = 1, message = "Maximum students must be at least 1")
    @Max(value = 50, message = "Maximum students cannot exceed 50")
    private Integer maxStudents;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
