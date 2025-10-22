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
import com.student_management_system.common.service.NotificationService;

import java.util.Collections;
import java.time.LocalDateTime;

import com.student_management_system.principal.dto.TeacherPerformanceDto;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.common.model.BudgetRequest;
import com.student_management_system.common.model.RequestStatus;
import com.student_management_system.common.repository.BudgetRequestRepository;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.student.repository.TimetableEntryRepository;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.repository.TimeSlotRepository;

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
    private final NotificationService notificationService;
    private final SubjectRepository subjectRepository;
    private final AcademicYearService academicYearService;
    private final TimetableEntryRepository timetableEntryRepository;
    private final TimeSlotRepository timeSlotRepository;

    public PrincipalService(AssignmentRepository assignmentRepository, AnnouncementRepository announcementRepository, UserRepository userRepository, BudgetRequestRepository budgetRequestRepository, NotificationService notificationService, SubjectRepository subjectRepository, AcademicYearService academicYearService, TimetableEntryRepository timetableEntryRepository, TimeSlotRepository timeSlotRepository) {
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.budgetRequestRepository = budgetRequestRepository;
        this.notificationService = notificationService;
        this.subjectRepository = subjectRepository;
        this.academicYearService = academicYearService;
        this.timetableEntryRepository = timetableEntryRepository;
        this.timeSlotRepository = timeSlotRepository;
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
        
        sendAnnouncementNotifications(title, content);
    }
    
    private void sendAnnouncementNotifications(String title, String content) {
        // Get all users to notify
        List<User> allUsers = userRepository.findAll();
        
        // Prepare notification message
        String notificationMessage = "New Announcement: " + content;
        
        for (User user : allUsers) {
            // Skip if user has no email
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                continue;
            }
            
            // Strategy Pattern in action: Choose notification strategy based on user role
            switch (user.getRole()) {
                case ROLE_STUDENT:
                case ROLE_PARENT:
                    // For students and parents: Send in-app notification (default strategy)
                    notificationService.sendInAppNotification(user.getEmail(), title, notificationMessage);
                    break;
                    
                case ROLE_TEACHER:
                case ROLE_STAFF:
                    // For teachers and staff: Send both email and in-app notifications
                    notificationService.sendEmailNotification(user.getEmail(), title, notificationMessage);
                    notificationService.sendInAppNotification(user.getEmail(), title, notificationMessage);
                    break;
                    
                case ROLE_ADMIN:
                case ROLE_PRINCIPAL:
                    // For admins and principals: Send via all available strategies (broadcast)
                    notificationService.broadcastNotification(user.getEmail(), title, notificationMessage);
                    break;
                    
                default:
                    // Default: In-app notification
                    notificationService.sendInAppNotification(user.getEmail(), title, notificationMessage);
                    break;
            }
        }
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
    
    // Get total number of students
    public long getTotalStudents() {
        return userRepository.countByRole(Role.ROLE_STUDENT);
    }
    
    // Get total number of teachers
    public long getTotalTeachers() {
        return userRepository.countByRole(Role.ROLE_TEACHER);
    }
    
    // Get total number of subjects
    public long getTotalSubjects() {
        // Count all subjects in the system
        return subjectRepository.count();
    }
    
    // Get current academic year
    public AcademicYear getCurrentAcademicYear() {
        return academicYearService.getCurrentAcademicYear().orElse(null);
    }
    
    // Get latest announcement
    public Announcement getLatestAnnouncement() {
        List<Announcement> announcements = announcementRepository.findAllByOrderByPublishedDateDesc();
        if (announcements != null && !announcements.isEmpty()) {
            return announcements.get(0);
        }
        return null;
    }
    
    // Get master timetable
    public List<Object> getMasterTimetable() {
        // Get all timetable entries from the database
        return new ArrayList<>(timetableEntryRepository.findAll());
    }
    
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Get time slots for grid display
    public List<Object> getTimeSlots() {
        // Get all time slots from database
        return new ArrayList<>(timeSlotRepository.findAll());
    }
    
    // Get week days
    public List<String> getWeekDays() {
        List<String> days = new ArrayList<>();
        days.add("MONDAY");
        days.add("TUESDAY");
        days.add("WEDNESDAY");
        days.add("THURSDAY");
        days.add("FRIDAY");
        days.add("SATURDAY");
        return days;
    }
    
    // Get timetable grid organized by day and time slot
    public Map<String, Object> getTimetableGrid() {
        Map<String, Object> grid = new java.util.HashMap<>();
        List<Object> allEntries = getMasterTimetable();
        
        // Organize entries by day_timeslot key
        for (Object entry : allEntries) {
            // This would need proper casting and organization
            // For now, return empty grid
        }
        
        return grid;
    }
}