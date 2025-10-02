package com.student_management_system.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransferStudentDto {
    @NotNull(message = "Student must be selected")
    private Long studentId;

    @NotNull(message = "Current classroom must be specified")
    private Long currentClassroomId;

    @NotNull(message = "Target classroom must be selected")
    private Long targetClassroomId;

    @Size(max = 500, message = "Transfer reason cannot exceed 500 characters")
    private String transferReason;

    @AssertTrue(message = "Target classroom must be different from current classroom")
    public boolean isDifferentClassroom() {
        return currentClassroomId == null || targetClassroomId == null || 
               !currentClassroomId.equals(targetClassroomId);
    }
}
