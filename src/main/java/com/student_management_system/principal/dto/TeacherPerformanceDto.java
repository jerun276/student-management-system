package com.student_management_system.principal.dto;

import lombok.Data;

@Data
public class TeacherPerformanceDto {
    private String teacherName;
    private long totalAssignmentsAssigned;
    private long assignmentsGraded;
    private double gradingCompletionRate; // A percentage
}