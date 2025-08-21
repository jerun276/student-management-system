package com.student_management_system.teacher.controller;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.teacher.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {

    private final TeacherService teacherService;

    public TeacherDashboardController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        List<Assignment> submittedAssignments = teacherService.getAssignmentsToGrade();
        model.addAttribute("submittedAssignments", submittedAssignments);
        return "teacher/dashboard";
    }

    @GetMapping("/assignments/{id}/grade")
    public String showGradeAssignmentForm(@PathVariable Long id, Model model) {
        Assignment assignment = teacherService.getAssignmentToGradeById(id);
        model.addAttribute("assignment", assignment);
        return "teacher/assignment-grade";
    }

    @PostMapping("/assignments/{id}/grade")
    public String processGradeAssignment(
            @PathVariable Long id,
            @RequestParam String grade,
            @RequestParam String feedback) {
        teacherService.gradeAssignment(id, grade, feedback);
        return "redirect:/teacher/dashboard";
    }
}