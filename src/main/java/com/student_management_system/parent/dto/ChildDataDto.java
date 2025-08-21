package com.student_management_system.parent.dto;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.teacher.model.AttendanceRecord;
import lombok.Data;

import java.util.List;

@Data
public class ChildDataDto {
    private String username;
    private List<Assignment> assignments;
    private List<AttendanceRecord> attendanceRecords;
}