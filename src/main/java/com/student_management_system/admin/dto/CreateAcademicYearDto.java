package com.student_management_system.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAcademicYearDto {
    @NotEmpty(message = "Academic year name cannot be empty")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year format must be YYYY-YYYY (e.g., 2024-2025)")
    private String name;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Academic year duration must be between 10 and 14 months")
    public boolean isValidDuration() {
        if (startDate == null || endDate == null) return true;
        long months = java.time.Period.between(startDate, endDate).toTotalMonths();
        return months >= 10 && months <= 14;
    }
}
