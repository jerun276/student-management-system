package com.student_management_system.student.controller;

import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.service.StudentService;
import com.student_management_system.student.model.Assignment;
import com.student_management_system.common.model.Enrollment;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.common.repository.EnrollmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(StudentDashboardController.class);
    private final StudentService studentService;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public StudentDashboardController(StudentService studentService, UserRepository userRepository,
            EnrollmentRepository enrollmentRepository) {
        this.studentService = studentService;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        logger.info("========== STUDENT DASHBOARD ACCESSED ==========");
        logger.info("Page: /student/dashboard");
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get current enrollment
        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByStudent(currentUser);
        Enrollment currentEnrollment = activeEnrollments.isEmpty() ? null : activeEnrollments.get(0);

        // Get timetable
        List<TimetableEntry> timetable = studentService.getStudentTimetable();

        // Get assignments
        List<Assignment> assignments = studentService.getStudentAssignments();

        // Get courses (subjects) from timetable entries
        List<Object> myCourses = studentService.getStudentSubjects();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentEnrollment", currentEnrollment);
        model.addAttribute("timetable", timetable);
        model.addAttribute("assignments", assignments);
        model.addAttribute("myCourses", myCourses);
        logger.info("========== RENDERING STUDENT DASHBOARD ==========");

        return "student/dashboard";
    }

    @GetMapping("/assignments/{id}")
    public String viewAssignment(@PathVariable Long id, Model model) {
        Assignment assignment = studentService.getAssignmentByIdForStudent(id);
        model.addAttribute("assignment", assignment);
        return "student/assignment-submit";
    }

    @PostMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, @RequestParam String submissionContent,
            RedirectAttributes redirectAttributes) {
        try {
            studentService.submitAssignment(id, submissionContent);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting assignment: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/assignments")
    public String viewAllAssignments(Model model) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get all assignments for the student
        List<Assignment> assignments = studentService.getStudentAssignments();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("assignments", assignments);

        return "student/assignments";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        logger.info("========== STUDENT PROFILE ACCESSED ==========");
        logger.info("Page: /student/profile");
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get current enrollment
        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByStudent(currentUser);
        Enrollment currentEnrollment = activeEnrollments.isEmpty() ? null : activeEnrollments.get(0);

        model.addAttribute("user", currentUser);
        model.addAttribute("currentEnrollment", currentEnrollment);

        return "student/profile";
    }

    @GetMapping("/subjects")
    public String getSubjects(Model model) {
        logger.info("========== STUDENT SUBJECTS ACCESSED ==========");
        logger.info("Page: /student/subjects");
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get current enrollment
        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByStudent(currentUser);
        Enrollment currentEnrollment = activeEnrollments.isEmpty() ? null : activeEnrollments.get(0);

        // Get all subjects/courses for the student
        List<Object> myCourses = studentService.getStudentSubjects();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentEnrollment", currentEnrollment);
        model.addAttribute("myCourses", myCourses);

        return "student/subjects";
    }

    @GetMapping("/attendance")
    public String viewAttendance(Model model) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Get current enrollment
        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByStudent(currentUser);
        Enrollment currentEnrollment = activeEnrollments.isEmpty() ? null : activeEnrollments.get(0);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentEnrollment", currentEnrollment);

        return "student/attendance";
    }
}