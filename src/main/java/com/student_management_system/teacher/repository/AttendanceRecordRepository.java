package com.student_management_system.teacher.repository;

import com.student_management_system.teacher.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    // Find all records for a specific subject on a specific date
    List<AttendanceRecord> findBySubjectIdAndDate(Long subjectId, LocalDate date);
}