package com.student_management_system.teacher.controller;

import com.student_management_system.student.model.Assignment;
import com.student_management_system.student.model.AssignmentStatus;
import com.student_management_system.student.model.Subject;
import com.student_management_system.student.repository.SubjectRepository;
import com.student_management_system.teacher.dto.AssignmentCreationDto;
import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import com.student_management_system.common.model.Classroom;
import com.student_management_system.common.repository.ClassroomRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher/assignments")
public class TeacherAssignmentController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;

    public TeacherAssignmentController(TeacherService teacherService, UserRepository userRepository, SubjectRepository subjectRepository, ClassroomRepository classroomRepository) {
        this.teacherService = teacherService;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    public String showAssignments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("Current teacher not found"));

        List<Subject> teacherSubjects = subjectRepository.findByTeachersContaining(teacher);
        List<Assignment> teacherAssignments = teacherService.getAssignmentsByTeacher(teacher);

        Map<Long, Integer> subjectAssignmentCounts = new HashMap<>();
        Map<Long, Integer> subjectPendingCounts = new HashMap<>();
        for (Subject subject : teacherSubjects) {
            long totalAssignments = teacherAssignments.stream().filter(a -> a.getSubject() != null && a.getSubject().getId().equals(subject.getId())).count();
            long pendingGrading = teacherAssignments.stream().filter(a -> a.getSubject() != null && a.getSubject().getId().equals(subject.getId()) && a.getStatus() == AssignmentStatus.SUBMITTED).count();
            subjectAssignmentCounts.put(subject.getId(), (int) totalAssignments);
            subjectPendingCounts.put(subject.getId(), (int) pendingGrading);
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("subjects", teacherSubjects);
        model.addAttribute("assignments", teacherAssignments);
        model.addAttribute("subjectAssignmentCounts", subjectAssignmentCounts);
        model.addAttribute("subjectPendingCounts", subjectPendingCounts);
        return "teacher/assignments";
    }

    @GetMapping("/create")
    public String showCreateAssignmentForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User teacher = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("Current teacher not found"));

        List<Subject> teacherSubjects = subjectRepository.findByTeachersContaining(teacher);
        List<Classroom> allClassrooms = classroomRepository.findAll();

        model.addAttribute("assignment", new AssignmentCreationDto());
        model.addAttribute("subjects", teacherSubjects);
        model.addAttribute("classrooms", allClassrooms);
        model.addAttribute("teacher", teacher);
        model.addAttribute("isEdit", false);
        return "teacher/create-assignment";
    }

    @PostMapping("/create")
    public String createAssignment(@Valid @ModelAttribute("assignment") AssignmentCreationDto assignmentDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User teacher = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("Current teacher not found"));
            List<Subject> teacherSubjects = subjectRepository.findByTeachersContaining(teacher);
            List<Classroom> allClassrooms = classroomRepository.findAll();
            model.addAttribute("subjects", teacherSubjects);
            model.addAttribute("classrooms", allClassrooms);
            model.addAttribute("teacher", teacher);
            model.addAttribute("isEdit", false);
            return "teacher/create-assignment";
        }
        try {
            teacherService.createAssignmentForSubject(assignmentDto);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/assignments";
    }

    @GetMapping("/{id}")
    public String viewAssignmentDetails(@PathVariable Long id, Model model) {
        Assignment assignment = teacherService.getAssignmentById(id);
        model.addAttribute("assignment", assignment);
        return "teacher/assignment-detail";
    }

    @GetMapping("/{id}/grade")
    public String showGradeAssignmentForm(@PathVariable Long id, Model model) {
        Assignment assignment = teacherService.getAssignmentById(id);
        model.addAttribute("assignment", assignment);
        return "teacher/assignment-grade";
    }

    @PostMapping("/{id}/grade")
    public String gradeAssignment(@PathVariable Long id, @RequestParam String grade, @RequestParam String feedback, RedirectAttributes redirectAttributes) {
        try {
            teacherService.gradeAssignment(id, grade, feedback);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment graded successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/assignments/" + id;
    }

    /*
    // ===================================================================
    // ASSIGNMENT TEMPLATE MANAGEMENT - TEMPORARILY DISABLED
    // ===================================================================

    @GetMapping("/templates")
    public String manageAssignmentTemplates(Model model) {
        // This functionality is temporarily disabled.
        return "redirect:/teacher/assignments";
    }

    @GetMapping("/templates/{originalTitle}/edit")
    public String editAssignmentTemplate(@PathVariable String originalTitle, @RequestParam Long subjectId, @RequestParam Long classroomId, Model model) {
        // This functionality is temporarily disabled.
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/templates/update")
    public String updateAssignmentTemplate(@RequestParam String originalTitle, @RequestParam Long originalSubjectId, @RequestParam Long originalClassroomId, @RequestParam String assignmentTitle, @RequestParam String description, @RequestParam String dueDate, @RequestParam Long subjectId, @RequestParam Long classroomId, RedirectAttributes redirectAttributes) {
        // This functionality is temporarily disabled.
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/templates/{originalTitle}/delete")
    public String deleteAssignmentTemplate(@PathVariable String originalTitle, @RequestParam Long subjectId, @RequestParam Long classroomId, RedirectAttributes redirectAttributes) {
        // This functionality is temporarily disabled.
        return "redirect:/teacher/assignments";
    }
    */
}
