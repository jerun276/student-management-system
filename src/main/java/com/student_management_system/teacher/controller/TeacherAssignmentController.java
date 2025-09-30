package com.student_management_system.teacher.controller;

import com.student_management_system.teacher.dto.AssignmentCreationDto;
import com.student_management_system.teacher.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/assignments")
public class TeacherAssignmentController {

    private final TeacherService teacherService;

    public TeacherAssignmentController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/create")
    public String showCreateAssignmentForm(Model model) {
        model.addAttribute("assignment", new AssignmentCreationDto());
        model.addAttribute("subjects", teacherService.getAllSubjects());
        model.addAttribute("students", teacherService.getStudentsForAttendance());
        return "teacher/create-assignment";
    }

    @PostMapping("/create")
    public String createAssignment(@Valid @ModelAttribute("assignment") AssignmentCreationDto assignmentDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("subjects", teacherService.getAllSubjects());
            model.addAttribute("students", teacherService.getStudentsForAttendance());
            return "teacher/create-assignment";
        }

        try {
            teacherService.createAssignment(assignmentDto);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/teacher/dashboard";
    }
}
