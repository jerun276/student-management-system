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

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;

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
    private final com.student_management_system.common.repository.ClassroomRepository classroomRepository;
    private final com.student_management_system.common.repository.EnrollmentRepository enrollmentRepository;

    public PrincipalService(AssignmentRepository assignmentRepository, AnnouncementRepository announcementRepository, UserRepository userRepository, BudgetRequestRepository budgetRequestRepository, NotificationService notificationService, SubjectRepository subjectRepository, AcademicYearService academicYearService, TimetableEntryRepository timetableEntryRepository, TimeSlotRepository timeSlotRepository, com.student_management_system.common.repository.ClassroomRepository classroomRepository, com.student_management_system.common.repository.EnrollmentRepository enrollmentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.budgetRequestRepository = budgetRequestRepository;
        this.notificationService = notificationService;
        this.subjectRepository = subjectRepository;
        this.academicYearService = academicYearService;
        this.timetableEntryRepository = timetableEntryRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.classroomRepository = classroomRepository;
        this.enrollmentRepository = enrollmentRepository;
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
    
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));
        announcementRepository.delete(announcement);
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
    
    // Get all users except admin
    public List<User> getAllUsersExceptAdmin() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getRole().toString().equals("ROLE_ADMIN"))
                .collect(Collectors.toList());
    }
    
    // Get user by ID
    public java.util.Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Get budget request by ID
    public java.util.Optional<BudgetRequest> getBudgetRequestById(Long id) {
        return budgetRequestRepository.findById(id);
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
        // List<Object> allEntries = getMasterTimetable();
        
        // Organize entries by day_timeslot key
        // for (Object entry : allEntries) {
        //     // This would need proper casting and organization
        //     // For now, return empty grid
        // }
        
        return grid;
    }
    
    // Export reports as CSV
    public String exportReportsAsCSV() {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("SUBJECT PERFORMANCE REPORT\n");
        csv.append("Subject,Graded Assignments,Average Grade\n");
        
        // Subject Performance Data
        List<SubjectPerformanceDto> performanceReport = getSubjectPerformanceReport();
        for (SubjectPerformanceDto report : performanceReport) {
            csv.append(report.getSubjectName()).append(",")
               .append(report.getGradedAssignmentsCount()).append(",")
               .append(report.getAverageGrade()).append("\n");
        }
        
        csv.append("\n\nTEACHER PERFORMANCE REPORT\n");
        csv.append("Teacher Name,Total Assignments,Graded,Completion Rate\n");
        
        // Teacher Performance Data
        List<TeacherPerformanceDto> teacherReport = getTeacherPerformanceReport();
        for (TeacherPerformanceDto report : teacherReport) {
            csv.append(report.getTeacherName()).append(",")
               .append(report.getTotalAssignmentsAssigned()).append(",")
               .append(report.getAssignmentsGraded()).append(",")
               .append(report.getGradingCompletionRate()).append("%\n");
        }
        
        return csv.toString();
    }
    
    // Export reports as JSON
    public String exportReportsAsJSON() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"reportGeneratedAt\": \"").append(LocalDateTime.now()).append("\",\n");
        
        json.append("  \"subjectPerformance\": [\n");
        List<SubjectPerformanceDto> performanceReport = getSubjectPerformanceReport();
        for (int i = 0; i < performanceReport.size(); i++) {
            SubjectPerformanceDto report = performanceReport.get(i);
            json.append("    {\n");
            json.append("      \"subject\": \"").append(report.getSubjectName()).append("\",\n");
            json.append("      \"gradedAssignments\": ").append(report.getGradedAssignmentsCount()).append(",\n");
            json.append("      \"averageGrade\": ").append(report.getAverageGrade()).append("\n");
            json.append("    }");
            if (i < performanceReport.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");
        
        json.append("  \"teacherPerformance\": [\n");
        List<TeacherPerformanceDto> teacherReport = getTeacherPerformanceReport();
        for (int i = 0; i < teacherReport.size(); i++) {
            TeacherPerformanceDto report = teacherReport.get(i);
            json.append("    {\n");
            json.append("      \"teacher\": \"").append(report.getTeacherName()).append("\",\n");
            json.append("      \"totalAssignments\": ").append(report.getTotalAssignmentsAssigned()).append(",\n");
            json.append("      \"graded\": ").append(report.getAssignmentsGraded()).append(",\n");
            json.append("      \"completionRate\": ").append(report.getGradingCompletionRate()).append("\n");
            json.append("    }");
            if (i < teacherReport.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        
        return json.toString();
    }
    
    // Export reports as PDF
    public byte[] exportReportsAsPDF() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        // Title
        Paragraph title = new Paragraph("School Reports")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        // Generated date
        Paragraph date = new Paragraph("Generated: " + LocalDateTime.now())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(date);
        
        document.add(new Paragraph("\n"));
        
        // Subject Performance Section
        Paragraph subjectTitle = new Paragraph("Subject Performance Report")
                .setFontSize(14)
                .setBold();
        document.add(subjectTitle);
        
        // Subject Performance Table
        Table subjectTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2}))
                .useAllAvailableWidth();
        
        subjectTable.addCell(new Cell().add(new Paragraph("Subject").setBold()));
        subjectTable.addCell(new Cell().add(new Paragraph("Graded Assignments").setBold()));
        subjectTable.addCell(new Cell().add(new Paragraph("Average Grade").setBold()));
        
        List<SubjectPerformanceDto> performanceReport = getSubjectPerformanceReport();
        for (SubjectPerformanceDto report : performanceReport) {
            subjectTable.addCell(new Cell().add(new Paragraph(report.getSubjectName())));
            subjectTable.addCell(new Cell().add(new Paragraph(String.valueOf(report.getGradedAssignmentsCount()))));
            subjectTable.addCell(new Cell().add(new Paragraph(report.getAverageGrade().toString())));
        }
        
        document.add(subjectTable);
        document.add(new Paragraph("\n"));
        
        // Teacher Performance Section
        Paragraph teacherTitle = new Paragraph("Teacher Performance Report")
                .setFontSize(14)
                .setBold();
        document.add(teacherTitle);
        
        // Teacher Performance Table
        Table teacherTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2}))
                .useAllAvailableWidth();
        
        teacherTable.addCell(new Cell().add(new Paragraph("Teacher Name").setBold()));
        teacherTable.addCell(new Cell().add(new Paragraph("Total Assignments").setBold()));
        teacherTable.addCell(new Cell().add(new Paragraph("Graded").setBold()));
        teacherTable.addCell(new Cell().add(new Paragraph("Completion Rate").setBold()));
        
        List<TeacherPerformanceDto> teacherReport = getTeacherPerformanceReport();
        for (TeacherPerformanceDto report : teacherReport) {
            teacherTable.addCell(new Cell().add(new Paragraph(report.getTeacherName())));
            teacherTable.addCell(new Cell().add(new Paragraph(String.valueOf(report.getTotalAssignmentsAssigned()))));
            teacherTable.addCell(new Cell().add(new Paragraph(String.valueOf(report.getAssignmentsGraded()))));
            teacherTable.addCell(new Cell().add(new Paragraph(report.getGradingCompletionRate() + "%")));
        }
        
        document.add(teacherTable);
        
        document.close();
        return outputStream.toByteArray();
    }
    
    // Export all users data as CSV
    public String exportUsersAsCSV() {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("USERS DATA REPORT\n");
        csv.append("ID,Username,Email,First Name,Last Name,Role,Status,Phone,Address\n");
        
        // Get all users
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            csv.append(user.getId()).append(",")
               .append(user.getUsername()).append(",")
               .append(user.getEmail()).append(",")
               .append(user.getFirstName() != null ? user.getFirstName() : "").append(",")
               .append(user.getLastName() != null ? user.getLastName() : "").append(",")
               .append(user.getRole()).append(",")
               .append(user.isEnabled() ? "Active" : "Inactive").append(",")
               .append(user.getPhoneNumber() != null ? user.getPhoneNumber() : "").append(",")
               .append(user.getAddress() != null ? user.getAddress() : "").append("\n");
        }
        
        return csv.toString();
    }
    
    // Export all users data as JSON
    public String exportUsersAsJSON() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"reportGeneratedAt\": \"").append(LocalDateTime.now()).append("\",\n");
        json.append("  \"users\": [\n");
        
        List<User> allUsers = userRepository.findAll();
        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            json.append("    {\n");
            json.append("      \"id\": ").append(user.getId()).append(",\n");
            json.append("      \"username\": \"").append(user.getUsername()).append("\",\n");
            json.append("      \"email\": \"").append(user.getEmail()).append("\",\n");
            json.append("      \"firstName\": \"").append(user.getFirstName() != null ? user.getFirstName() : "").append("\",\n");
            json.append("      \"lastName\": \"").append(user.getLastName() != null ? user.getLastName() : "").append("\",\n");
            json.append("      \"role\": \"").append(user.getRole()).append("\",\n");
            json.append("      \"status\": \"").append(user.isEnabled() ? "Active" : "Inactive").append("\",\n");
            json.append("      \"phone\": \"").append(user.getPhoneNumber() != null ? user.getPhoneNumber() : "").append("\",\n");
            json.append("      \"address\": \"").append(user.getAddress() != null ? user.getAddress() : "").append("\"\n");
            json.append("    }");
            if (i < allUsers.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}\n");
        
        return json.toString();
    }
    
    // Export all users data as PDF
    public byte[] exportUsersAsPDF() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        // Title
        Paragraph title = new Paragraph("Users Data Report")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        // Generated date
        Paragraph date = new Paragraph("Generated: " + LocalDateTime.now())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(date);
        
        document.add(new Paragraph("\n"));
        
        // Users Table
        Table usersTable = new Table(UnitValue.createPercentArray(new float[]{1f, 2f, 2.5f, 1.5f, 1.5f, 1.5f, 1f, 1.5f}))
                .useAllAvailableWidth();
        
        usersTable.addCell(new Cell().add(new Paragraph("ID").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("Username").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("Email").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("First Name").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("Last Name").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("Role").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("Status").setBold()));
        usersTable.addCell(new Cell().add(new Paragraph("Phone").setBold()));
        
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            usersTable.addCell(new Cell().add(new Paragraph(String.valueOf(user.getId()))));
            usersTable.addCell(new Cell().add(new Paragraph(user.getUsername())));
            usersTable.addCell(new Cell().add(new Paragraph(user.getEmail())));
            usersTable.addCell(new Cell().add(new Paragraph(user.getFirstName() != null ? user.getFirstName() : "")));
            usersTable.addCell(new Cell().add(new Paragraph(user.getLastName() != null ? user.getLastName() : "")));
            usersTable.addCell(new Cell().add(new Paragraph(user.getRole().toString())));
            usersTable.addCell(new Cell().add(new Paragraph(user.isEnabled() ? "Active" : "Inactive")));
            usersTable.addCell(new Cell().add(new Paragraph(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")));
        }
        
        document.add(usersTable);
        
        document.close();
        return outputStream.toByteArray();
    }
    
    // Get all classrooms with teacher and student details
    public List<Map<String, Object>> getClassroomsWithDetails() {
        List<Map<String, Object>> classroomDetails = new ArrayList<>();
        
        // Get all classrooms from database
        List<com.student_management_system.common.model.Classroom> allClassrooms = classroomRepository.findAll();
        
        for (com.student_management_system.common.model.Classroom classroom : allClassrooms) {
            Map<String, Object> classInfo = new java.util.HashMap<>();
            classInfo.put("id", classroom.getId());
            classInfo.put("name", classroom.getName());
            classInfo.put("gradeLevel", classroom.getGradeLevel() != null ? classroom.getGradeLevel().getName() : "N/A");
            classInfo.put("medium", classroom.getMedium() != null ? classroom.getMedium().toString() : "N/A");
            classInfo.put("academicYear", classroom.getAcademicYear() != null ? classroom.getAcademicYear().getName() : "N/A");
            
            // Get class teacher
            if (classroom.getClassTeacher() != null) {
                classInfo.put("teacher", classroom.getClassTeacher().getUsername());
                classInfo.put("teacherEmail", classroom.getClassTeacher().getEmail());
            } else {
                classInfo.put("teacher", "Not Assigned");
                classInfo.put("teacherEmail", "N/A");
            }
            
            // Get enrolled students from enrollments (not assignments)
            List<User> enrolledStudents = enrollmentRepository.findStudentsByClassroom(classroom);
            
            classInfo.put("studentCount", enrolledStudents.size());
            classInfo.put("students", enrolledStudents);
            
            classroomDetails.add(classInfo);
        }
        
        return classroomDetails;
    }
    
    // Get grades for a specific classroom
    public List<Map<String, Object>> getGradesForClassroom(Long classroomId) {
        List<Map<String, Object>> grades = new ArrayList<>();
        
        // Get all assignments for this classroom
        List<Assignment> classroomAssignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getClassroom() != null && a.getClassroom().getId().equals(classroomId))
                .collect(Collectors.toList());
        
        if (classroomAssignments.isEmpty()) {
            return grades;
        }
        
        // Get unique students from assignments
        List<User> enrolledStudents = classroomAssignments.stream()
                .map(Assignment::getUser)
                .distinct()
                .collect(Collectors.toList());
        
        for (User student : enrolledStudents) {
            Map<String, Object> studentGrades = new java.util.HashMap<>();
            studentGrades.put("studentId", student.getId());
            studentGrades.put("studentName", student.getUsername());
            studentGrades.put("studentEmail", student.getEmail());
            
            List<Map<String, String>> assignmentGrades = new ArrayList<>();
            for (Assignment assignment : classroomAssignments) {
                if (assignment.getUser() != null && assignment.getUser().getId().equals(student.getId())) {
                    Map<String, String> assignmentGrade = new java.util.HashMap<>();
                    assignmentGrade.put("assignmentTitle", assignment.getTitle());
                    assignmentGrade.put("grade", assignment.getGrade() != null ? assignment.getGrade() : "Not Graded");
                    assignmentGrade.put("status", assignment.getStatus().toString());
                    assignmentGrades.add(assignmentGrade);
                }
            }
            
            studentGrades.put("assignments", assignmentGrades);
            grades.add(studentGrades);
        }
        
        return grades;
    }
}