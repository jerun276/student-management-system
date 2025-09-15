package com.student_management_system.principal.service;

import com.student_management_system.principal.dto.SubjectPerformanceDto;
import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.repository.AssignmentRepository;
import org.springframework.stereotype.Service;
import com.student_management_system.principal.model.Announcement;
import com.student_management_system.principal.repository.AnnouncementRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PrincipalService {

    private final AssignmentRepository assignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public PrincipalService(AssignmentRepository assignmentRepository, AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    public List<SubjectPerformanceDto> getSubjectPerformanceReport() {
        // 1. Fetch all assignments that have been graded.
        List<Assignment> gradedAssignments = assignmentRepository.findByStatus(AssignmentStatus.GRADED);

        // 2. Group these assignments by their subject name.
        Map<String, List<Assignment>> assignmentsBySubject = gradedAssignments.stream()
                .collect(Collectors.groupingBy(assignment -> assignment.getSubject().getName()));

        // 3. Process each group to calculate the average grade.
        return assignmentsBySubject.entrySet().stream()
                .map(entry -> {
                    String subjectName = entry.getKey();
                    List<Assignment> assignments = entry.getValue();
                    long gradedCount = assignments.size();
                    BigDecimal averageGrade = calculateAverageGrade(assignments);

                    return new SubjectPerformanceDto(subjectName, gradedCount, averageGrade);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateAverageGrade(List<Assignment> assignments) {
        BigDecimal totalScore = BigDecimal.ZERO;
        int validGradesCount = 0;

        for (Assignment assignment : assignments) {
            // This logic assumes grades are numeric (e.g., "85", "92.5").
            // It will skip non-numeric grades like "A+" or "Pass".
            try {
                BigDecimal grade = new BigDecimal(assignment.getGrade().replaceAll("[^\\d.]", "")); // Clean the grade string
                totalScore = totalScore.add(grade);
                validGradesCount++;
            } catch (NumberFormatException | NullPointerException e) {
                // Ignore grades that are not valid numbers
            }
        }

        if (validGradesCount == 0) {
            return BigDecimal.ZERO;
        }
        // Calculate the average, scaling to 2 decimal places.
        return totalScore.divide(new BigDecimal(validGradesCount), 2, RoundingMode.HALF_UP);
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAllByOrderByPublishedDateDesc();
        if (announcements == null) {
            return Collections.emptyList();
        }
        return announcements;
    }

    @Transactional
    public void createAnnouncement(String title, String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User principal = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current principal not found"));

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPublishedBy(principal);
        announcement.setPublishedDate(LocalDateTime.now());

        announcementRepository.save(announcement);
    }
}