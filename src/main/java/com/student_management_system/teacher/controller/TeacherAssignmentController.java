package com.student_management_system.teacher.controller;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.Subject;
import com.student_management_system.teacher.dto.AssignmentDto;
import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/teacher/assignments")
public class TeacherAssignmentController {

    private final TeacherService teacherService;
    private final StudentService studentService;

    public TeacherAssignmentController(TeacherService teacherService, StudentService studentService) {
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    @GetMapping
    public String listAssignments(Model model, Principal principal) {
        try {
            List<Assignment> assignments = teacherService.getAssignmentsByTeacher(principal.getName());
            model.addAttribute("assignments", assignments);
            return "teacher/assignments/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading assignments: " + e.getMessage());
            return "teacher/assignments/list";
        }
    }

    @GetMapping("/create")
    public String showCreateAssignmentForm(Model model, Principal principal) {
        try {
            AssignmentDto assignmentDto = new AssignmentDto();
            List<Subject> subjects = teacherService.getSubjectsByTeacher(principal.getName());
            
            model.addAttribute("assignmentDto", assignmentDto);
            model.addAttribute("subjects", subjects);
            return "teacher/assignments/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    @PostMapping("/create")
    public String createAssignment(@Valid @ModelAttribute("assignmentDto") AssignmentDto assignmentDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            try {
                List<Subject> subjects = teacherService.getSubjectsByTeacher(principal.getName());
                model.addAttribute("subjects", subjects);
                return "teacher/assignments/create";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error loading subjects: " + e.getMessage());
                return "redirect:/teacher/assignments";
            }
        }

        try {
            teacherService.createAssignment(assignmentDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Assignment created successfully!");
            return "redirect:/teacher/assignments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create assignment: " + e.getMessage());
            return "redirect:/teacher/assignments/create";
        }
    }

    @GetMapping("/{id}")
    public String viewAssignment(@PathVariable Long id, Model model, Principal principal) {
        try {
            Assignment assignment = teacherService.getAssignmentById(id, principal.getName());
            List<Assignment> submissions = teacherService.getSubmissionsByAssignment(id);
            
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", submissions);
            return "teacher/assignments/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Assignment not found or access denied");
            return "redirect:/teacher/assignments";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditAssignmentForm(@PathVariable Long id, Model model, Principal principal) {
        try {
            Assignment assignment = teacherService.getAssignmentById(id, principal.getName());
            AssignmentDto assignmentDto = teacherService.convertToDto(assignment);
            List<Subject> subjects = teacherService.getSubjectsByTeacher(principal.getName());
            
            model.addAttribute("assignmentDto", assignmentDto);
            model.addAttribute("subjects", subjects);
            model.addAttribute("assignmentId", id);
            return "teacher/assignments/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Assignment not found or access denied");
            return "redirect:/teacher/assignments";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateAssignment(@PathVariable Long id,
                                 @Valid @ModelAttribute("assignmentDto") AssignmentDto assignmentDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            try {
                List<Subject> subjects = teacherService.getSubjectsByTeacher(principal.getName());
                model.addAttribute("subjects", subjects);
                model.addAttribute("assignmentId", id);
                return "teacher/assignments/edit";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error loading subjects: " + e.getMessage());
                return "redirect:/teacher/assignments";
            }
        }

        try {
            teacherService.updateAssignment(id, assignmentDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Assignment updated successfully!");
            return "redirect:/teacher/assignments/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update assignment: " + e.getMessage());
            return "redirect:/teacher/assignments/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, 
                                 Principal principal, 
                                 RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteAssignment(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Assignment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete assignment: " + e.getMessage());
        }
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/{id}/publish")
    public String publishAssignment(@PathVariable Long id, 
                                  Principal principal, 
                                  RedirectAttributes redirectAttributes) {
        try {
            teacherService.publishAssignment(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Assignment published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish assignment: " + e.getMessage());
        }
        return "redirect:/teacher/assignments/" + id;
    }

    @PostMapping("/{id}/unpublish")
    public String unpublishAssignment(@PathVariable Long id, 
                                    Principal principal, 
                                    RedirectAttributes redirectAttributes) {
        try {
            teacherService.unpublishAssignment(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Assignment unpublished successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to unpublish assignment: " + e.getMessage());
        }
        return "redirect:/teacher/assignments/" + id;
    }
}
