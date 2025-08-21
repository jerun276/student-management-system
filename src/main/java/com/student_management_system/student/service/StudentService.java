package com.student_management_system.student.service;

import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.repository.TimetableEntryRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.repository.AssignmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public StudentService(TimetableEntryRepository timetableEntryRepository, UserRepository userRepository, AssignmentRepository assignmentRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<TimetableEntry> getStudentTimetable() {
        // Get the currently logged-in user's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Find the user in the database
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));

        // Use our custom repository method to fetch the timetable
        return timetableEntryRepository.findByUserOrderByDayOfWeekAscStartTimeAsc(user);
    }

    public List<Assignment> getStudentAssignments() {
        User user = getCurrentUser();
        return assignmentRepository.findByUserOrderByDueDateAsc(user);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found in database"));
    }

    public Assignment getAssignmentByIdForStudent(Long assignmentId) {
        User currentUser = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        // SECURITY CHECK: Ensure the student requesting the assignment is the one it's assigned to
        if (!assignment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this assignment.");
        }
        return assignment;
    }

    public void submitAssignment(Long assignmentId, String submissionContent) {
        // Use the existing method to get the assignment securely
        Assignment assignment = getAssignmentByIdForStudent(assignmentId);

        // Don't allow submission if it's not pending
        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new IllegalStateException("This assignment has already been submitted or graded.");
        }

        assignment.setSubmissionText(submissionContent);
        assignment.setSubmissionDate(LocalDateTime.now());
        assignment.setStatus(AssignmentStatus.SUBMITTED);

        assignmentRepository.save(assignment);
    }
}