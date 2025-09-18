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
import com.student_management_system.principal.dto.AnnouncementDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.time.LocalDateTime;

import com.student_management_system.principal.dto.TeacherPerformanceDto;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.common.model.BudgetRequest;
import com.student_management_system.common.model.RequestStatus;
import com.student_management_system.common.repository.BudgetRequestRepository;

import java.util.ArrayList;

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
    private final BudgetRequestRepository budgetRequestRepository;

    public PrincipalService(AssignmentRepository assignmentRepository, AnnouncementRepository announcementRepository, UserRepository userRepository, BudgetRequestRepository budgetRequestRepository) {
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.budgetRequestRepository = budgetRequestRepository;
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

    // Generate the Teacher Performance Report
    public List<TeacherPerformanceDto> getTeacherPerformanceReport() {
        // 1. Get all teachers
        List<User> teachers = userRepository.findByRole(Role.ROLE_TEACHER);

        // 2. Get all assignments
        List<Assignment> allAssignments = assignmentRepository.findAll();

        List<TeacherPerformanceDto> report = new ArrayList<>();

        // 3. For each teacher, calculate their stats
        for (User teacher : teachers) {
            TeacherPerformanceDto dto = new TeacherPerformanceDto();
            dto.setTeacherName(teacher.getUsername());

            // Filter assignments for the current teacher
            List<Assignment> teacherAssignments = allAssignments.stream()
                    .filter(a -> a.getTeacher() != null && a.getTeacher().getId().equals(teacher.getId()))
                    .collect(Collectors.toList());

            long totalAssigned = teacherAssignments.size();
            long gradedCount = teacherAssignments.stream()
                    .filter(a -> a.getStatus() == AssignmentStatus.GRADED)
                    .count();

            dto.setTotalAssignmentsAssigned(totalAssigned);
            dto.setAssignmentsGraded(gradedCount);

            if (totalAssigned > 0) {
                double completionRate = ((double) gradedCount / totalAssigned) * 100.0;
                dto.setGradingCompletionRate(completionRate);
            } else {
                dto.setGradingCompletionRate(0.0);
            }

            report.add(dto);
        }

        return report;
    }

    // For budget approval
    public List<BudgetRequest> getPendingBudgetRequests() {
        return budgetRequestRepository.findByStatusOrderByRequestDateDesc(RequestStatus.PENDING);
    }

    @Transactional
    public void approveBudgetRequest(Long requestId) {
        BudgetRequest request = budgetRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.APPROVED);
        budgetRequestRepository.save(request);
    }

    @Transactional
    public void rejectBudgetRequest(Long requestId) {
        BudgetRequest request = budgetRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.REJECTED);
        budgetRequestRepository.save(request);
    }

    // ===== ENHANCED ANNOUNCEMENT CRUD OPERATIONS =====

    /**
     * Create announcement from DTO
     */
    @Transactional
    public Announcement createAnnouncement(AnnouncementDto announcementDto, String principalUsername) {
        User principal = userRepository.findByUsername(principalUsername)
                .orElseThrow(() -> new RuntimeException("Principal not found"));

        Announcement announcement = new Announcement();
        announcement.setTitle(announcementDto.getTitle());
        announcement.setContent(announcementDto.getContent());
        announcement.setPublishedBy(principal);
        announcement.setPublishedDate(LocalDateTime.now());

        return announcementRepository.save(announcement);
    }

    /**
     * Get announcement by ID
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));
    }

    /**
     * Update announcement
     */
    @Transactional
    public Announcement updateAnnouncement(Long id, AnnouncementDto announcementDto, String principalUsername) {
        Announcement announcement = getAnnouncementById(id);
        
        // Verify ownership (optional - principals might edit each other's announcements)
        announcement.setTitle(announcementDto.getTitle());
        announcement.setContent(announcementDto.getContent());
        
        return announcementRepository.save(announcement);
    }

    /**
     * Delete announcement
     */
    @Transactional
    public void deleteAnnouncement(Long id, String principalUsername) {
        Announcement announcement = getAnnouncementById(id);
        announcementRepository.delete(announcement);
    }

    /**
     * Archive announcement
     */
    @Transactional
    public void archiveAnnouncement(Long id, String principalUsername) {
        Announcement announcement = getAnnouncementById(id);
        announcement.setActive(false);
        announcement.setLastModifiedDate(LocalDateTime.now());
        announcementRepository.save(announcement);
    }

    /**
     * Unarchive announcement
     */
    @Transactional
    public void unarchiveAnnouncement(Long id, String principalUsername) {
        Announcement announcement = getAnnouncementById(id);
        announcement.setActive(true);
        announcement.setLastModifiedDate(LocalDateTime.now());
        announcementRepository.save(announcement);
    }

    /**
     * Pin announcement
     */
    @Transactional
    public void pinAnnouncement(Long id, String principalUsername) {
        Announcement announcement = getAnnouncementById(id);
        announcement.setPinned(true);
        announcement.setLastModifiedDate(LocalDateTime.now());
        announcementRepository.save(announcement);
    }

    /**
     * Unpin announcement
     */
    @Transactional
    public void unpinAnnouncement(Long id, String principalUsername) {
        Announcement announcement = getAnnouncementById(id);
        announcement.setPinned(false);
        announcement.setLastModifiedDate(LocalDateTime.now());
        announcementRepository.save(announcement);
    }

    /**
     * Get paginated announcements
     */
    public Page<Announcement> getAllAnnouncementsPaginated(Pageable pageable) {
        return announcementRepository.findAll(pageable);
    }

    /**
     * Search announcements
     */
    public Page<Announcement> searchAnnouncements(String searchTerm, Pageable pageable) {
        return announcementRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByPublishedDateDesc(
            searchTerm, searchTerm, pageable);
    }

    /**
     * Get announcements by category
     */
    public Page<Announcement> getAnnouncementsByCategory(String category, Pageable pageable) {
        return announcementRepository.findByCategoryOrderByPublishedDateDesc(category, pageable);
    }

    /**
     * Get archived announcements
     */
    public Page<Announcement> getArchivedAnnouncements(Pageable pageable) {
        return announcementRepository.findByIsActiveFalseOrderByPublishedDateDesc(pageable);
    }

    /**
     * Get announcement categories
     */
    public List<String> getAnnouncementCategories() {
        return List.of("General", "Academic", "Events", "Emergency", "Sports", "Exam", "Holiday");
    }

    /**
     * Convert Announcement to DTO
     */
    public AnnouncementDto convertAnnouncementToDto(Announcement announcement) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setPublishedDate(announcement.getPublishedDate());
        dto.setPublishedByName(announcement.getPublishedBy().getUsername());
        return dto;
    }
}