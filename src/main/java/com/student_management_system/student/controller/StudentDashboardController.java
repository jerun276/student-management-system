package com.student_management_system.student.controller;

import com.student_management_system.student.model.TimetableEntry;
import com.student_management_system.student.service.StudentService;
import com.student_management_system.student.model.Assignment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {
    private final StudentService studentService;

    // Inject the StudentService
    public StudentDashboardController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        // Call the service to get the timetable
        List<TimetableEntry> timetable = studentService.getStudentTimetable();
        model.addAttribute("timetable", timetable);

        // Fetch assignments
        List<Assignment> assignments = studentService.getStudentAssignments();
        model.addAttribute("assignments", assignments);

        return "student/dashboard";
    }

    @GetMapping("/assignments/{id}")
    public String viewAssignment(@PathVariable Long id, Model model) {
        Assignment assignment = studentService.getAssignmentByIdForStudent(id);
        model.addAttribute("assignment", assignment);
        return "student/assignment-submit";
    }

    @PostMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, @RequestParam String submissionContent) {
        studentService.submitAssignment(id, submissionContent);
        return "redirect:/student/dashboard"; // Redirect back to the dashboard
    }
}