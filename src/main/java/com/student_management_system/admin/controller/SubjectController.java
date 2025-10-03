package com.student_management_system.admin.controller;

import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.common.model.GradeLevel;
import com.student_management_system.common.repository.GradeLevelRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Controller for admin subject management operations
 */
@Controller
@RequestMapping("/admin/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private GradeLevelRepository gradeLevelRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Display all subjects with filtering options
     */
    @GetMapping
    public String listSubjects(@RequestParam(required = false) Long gradeLevelId, Model model) {
        List<Subject> subjects;
        
        if (gradeLevelId != null) {
            subjects = subjectRepository.findByGradeLevelIdAndIsActiveTrue(gradeLevelId);
            model.addAttribute("selectedGradeLevelId", gradeLevelId);
        } else {
            subjects = subjectRepository.findByIsActiveTrue();
        }
        
        model.addAttribute("subjects", subjects);
        model.addAttribute("gradeLevels", gradeLevelRepository.findAllByOrderByLevelAsc());
        
        return "admin/subjects/list";
    }

    /**
     * Show form to create a new subject
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Subject subject = new Subject();
        subject.setActive(true); // Initialize with active status
        model.addAttribute("subject", subject);
        model.addAttribute("gradeLevels", gradeLevelRepository.findAllByOrderByLevelAsc());
        model.addAttribute("teachers", userRepository.findByRole(Role.ROLE_TEACHER));
        
        return "admin/subjects/create";
    }

    /**
     * Create a new subject
     */
    @PostMapping("/create")
    public String createSubject(@ModelAttribute Subject subject, 
                               @RequestParam Long gradeLevelId,
                               @RequestParam(required = false) List<Long> teacherIds,
                               RedirectAttributes redirectAttributes) {
        try {
            // Set grade level
            Optional<GradeLevel> gradeLevel = gradeLevelRepository.findById(gradeLevelId);
            if (gradeLevel.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid grade level selected.");
                return "redirect:/admin/subjects/create";
            }
            
            subject.setGradeLevel(gradeLevel.get());
            subject.setActive(true);
            
            // Check for duplicate subject code
            if (subjectRepository.findBySubjectCode(subject.getSubjectCode()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Subject code already exists. Please use a different code.");
                return "redirect:/admin/subjects/create";
            }
            
            // Save subject first
            Subject savedSubject = subjectRepository.save(subject);
            
            // Assign teachers if provided
            if (teacherIds != null && !teacherIds.isEmpty()) {
                for (Long teacherId : teacherIds) {
                    Optional<User> teacher = userRepository.findById(teacherId);
                    if (teacher.isPresent()) {
                        savedSubject.addTeacher(teacher.get());
                    }
                }
                subjectRepository.save(savedSubject);
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Subject '" + subject.getName() + "' created successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error creating subject: " + e.getMessage());
            return "redirect:/admin/subjects/create";
        }
        
        return "redirect:/admin/subjects";
    }

    /**
     * Show form to edit an existing subject
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Subject> subject = subjectRepository.findById(id);
        
        if (subject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Subject not found.");
            return "redirect:/admin/subjects";
        }
        
        model.addAttribute("subject", subject.get());
        model.addAttribute("gradeLevels", gradeLevelRepository.findAllByOrderByLevelAsc());
        model.addAttribute("teachers", userRepository.findByRole(Role.ROLE_TEACHER));
        model.addAttribute("assignedTeachers", subject.get().getTeachers());
        
        return "admin/subjects/edit";
    }

    /**
     * Update an existing subject
     */
    @PostMapping("/{id}/edit")
    public String updateSubject(@PathVariable Long id,
                               @ModelAttribute Subject subject,
                               @RequestParam Long gradeLevelId,
                               @RequestParam(required = false) List<Long> teacherIds,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Subject> existingSubject = subjectRepository.findById(id);
            if (existingSubject.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Subject not found.");
                return "redirect:/admin/subjects";
            }
            
            Subject subjectToUpdate = existingSubject.get();
            
            // Check for duplicate subject code (excluding current subject)
            Optional<Subject> duplicateCode = subjectRepository.findBySubjectCode(subject.getSubjectCode());
            if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Subject code already exists. Please use a different code.");
                return "redirect:/admin/subjects/" + id + "/edit";
            }
            
            // Update basic fields
            subjectToUpdate.setName(subject.getName());
            subjectToUpdate.setSubjectCode(subject.getSubjectCode());
            subjectToUpdate.setDescription(subject.getDescription());
            
            // Update grade level
            Optional<GradeLevel> gradeLevel = gradeLevelRepository.findById(gradeLevelId);
            if (gradeLevel.isPresent()) {
                subjectToUpdate.setGradeLevel(gradeLevel.get());
            }
            
            // Clear existing teacher assignments using helper method
            for (User currentTeacher : new HashSet<>(subjectToUpdate.getTeachers())) {
                subjectToUpdate.removeTeacher(currentTeacher);
            }
            
            // Add new teacher assignments using helper method
            if (teacherIds != null && !teacherIds.isEmpty()) {
                for (Long teacherId : teacherIds) {
                    Optional<User> teacher = userRepository.findById(teacherId);
                    if (teacher.isPresent()) {
                        subjectToUpdate.addTeacher(teacher.get());
                    }
                }
            }
            
            subjectRepository.save(subjectToUpdate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Subject '" + subjectToUpdate.getName() + "' updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating subject: " + e.getMessage());
            return "redirect:/admin/subjects/" + id + "/edit";
        }
        
        return "redirect:/admin/subjects";
    }

    /**
     * View subject details
     */
    @GetMapping("/{id}")
    public String viewSubject(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Subject> subject = subjectRepository.findById(id);
        
        if (subject.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Subject not found.");
            return "redirect:/admin/subjects";
        }
        
        model.addAttribute("subject", subject.get());
        
        return "admin/subjects/view";
    }

    /**
     * Deactivate a subject (soft delete)
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Subject> subject = subjectRepository.findById(id);
            if (subject.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Subject not found.");
                return "redirect:/admin/subjects";
            }
            
            Subject subjectToDeactivate = subject.get();
            subjectToDeactivate.setActive(false);
            subjectRepository.save(subjectToDeactivate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Subject '" + subjectToDeactivate.getName() + "' deactivated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deactivating subject: " + e.getMessage());
        }
        
        return "redirect:/admin/subjects";
    }

    /**
     * Reactivate a subject
     */
    @PostMapping("/{id}/activate")
    public String activateSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Subject> subject = subjectRepository.findById(id);
            if (subject.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Subject not found.");
                return "redirect:/admin/subjects";
            }
            
            Subject subjectToActivate = subject.get();
            subjectToActivate.setActive(true);
            subjectRepository.save(subjectToActivate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Subject '" + subjectToActivate.getName() + "' activated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error activating subject: " + e.getMessage());
        }
        
        return "redirect:/admin/subjects";
    }
}
