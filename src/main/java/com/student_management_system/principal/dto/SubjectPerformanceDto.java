package com.student_management_system.principal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectPerformanceDto {
    private String subjectName;
    private long gradedAssignmentsCount;
    private BigDecimal averageGrade;
}