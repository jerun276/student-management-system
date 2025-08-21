package com.student_management_system.teacher.service;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.repository.AssignmentRepository;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.teacher.model.AttendanceRecord;
import com.student_management_system.teacher.model.AttendanceStatus;
import com.student_management_system.teacher.repository.AttendanceRecordRepository;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public TeacherService(AssignmentRepository assignmentRepository, SubjectRepository subjectRepository,
                          UserRepository userRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    // For now, we get all submitted assignments. Later, we can filter by teacher.
    public List<Assignment> getAssignmentsToGrade() {
        return assignmentRepository.findByStatusOrderBySubmissionDateDesc(AssignmentStatus.SUBMITTED);
    }

    public Assignment getAssignmentToGradeById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
    }

    public void gradeAssignment(Long assignmentId, String grade, String feedback) {
        Assignment assignment = getAssignmentToGradeById(assignmentId);

        // Optional: Add a check to ensure it's in the SUBMITTED state
        if (assignment.getStatus() != AssignmentStatus.SUBMITTED) {
            throw new IllegalStateException("This assignment is not in a submittable state for grading.");
        }

        assignment.setGrade(grade);
        assignment.setFeedback(feedback);
        assignment.setStatus(AssignmentStatus.GRADED);

        assignmentRepository.save(assignment);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<User> getStudentsForAttendance() {
        return userRepository.findByRole(Role.ROLE_STUDENT);
    }

    public Map<Long, AttendanceStatus> getAttendanceRecordsForSubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRecordRepository.findBySubjectIdAndDate(subjectId, date)
                .stream()
                .collect(Collectors.toMap(
                        record -> record.getStudent().getId(),
                        AttendanceRecord::getStatus
                ));
    }

    @Transactional
    public void saveAttendance(Long subjectId, LocalDate date, Map<String, String> attendanceData) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        for (Map.Entry<String, String> entry : attendanceData.entrySet()) {
            if (!entry.getKey().matches("\\d+")) {
                continue;
            }

            Long studentId = Long.parseLong(entry.getKey());
            AttendanceStatus status = AttendanceStatus.valueOf(entry.getValue());

            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // "Upsert" logic: Update existing record or create a new one
            List<AttendanceRecord> existingRecords = attendanceRecordRepository.findBySubjectIdAndDate(subjectId, date);
            AttendanceRecord record = existingRecords.stream()
                    .filter(r -> r.getStudent().getId().equals(studentId))
                    .findFirst()
                    .orElse(new AttendanceRecord());

            record.setStudent(student);
            record.setSubject(subject);
            record.setDate(date);
            record.setStatus(status);

            attendanceRecordRepository.save(record);
        }
    }
}