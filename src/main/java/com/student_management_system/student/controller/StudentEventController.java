package com.student_management_system.student.controller;

import com.student_management_system.staff.service.StaffService;
import com.student_management_system.student.service.StudentService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/events")
public class StudentEventController {

    private final StudentService studentService;
    private final StaffService staffService; // Used for registration logic
    private final UserRepository userRepository;

    public StudentEventController(StudentService studentService, StaffService staffService, UserRepository userRepository) {
        this.studentService = studentService;
        this.staffService = staffService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listEventsForStudents(Model model) {
        model.addAttribute("events", studentService.getAllEvents());
        return "student/events-list";
    }

    @PostMapping("/{id}/register")
    public String registerForEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User student = getCurrentUser();
            staffService.registerStudentForEvent(id, student);
            redirectAttributes.addFlashAttribute("successMessage", "You have been successfully registered for the event!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }
        return "redirect:/student/events";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
    }
}