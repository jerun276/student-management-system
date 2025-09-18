package com.student_management_system.teacher.controller;

import com.student_management_system.teacher.dto.GradeDto;
import com.student_management_system.teacher.model.Grade;
import com.student_management_system.teacher.model.GradeType;
import com.student_management_system.teacher.service.TeacherService;
import com.student_management_system.student.model.Subject;
import com.student_management_system.user_management.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/teacher/grades")
public class TeacherGradeController {

    private final TeacherService teacherService;

    public TeacherGradeController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String listGrades(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "gradedDate") String sortBy,
                           @RequestParam(defaultValue = "desc") String sortDir,
                           @RequestParam(required = false) Long subjectId,
                           @RequestParam(required = false) String semester,
                           Model model,
                           Principal principal) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Grade> gradePage = teacherService.getGradesByTeacher(principal.getName(), subjectId, semester, pageable);
            
            model.addAttribute("gradePage", gradePage);
            model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
            model.addAttribute("semesters", teacherService.getAvailableSemesters());
            model.addAttribute("currentSubjectId", subjectId);
            model.addAttribute("currentSemester", semester);
            
            return "teacher/grades/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading grades: " + e.getMessage());
            return "teacher/grades/list";
        }
    }

    @GetMapping("/create")
    public String showCreateGradeForm(Model model, Principal principal) {
        GradeDto gradeDto = new GradeDto();
        model.addAttribute("gradeDto", gradeDto);
        model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
        model.addAttribute("gradeTypes", GradeType.values());
        model.addAttribute("students", teacherService.getAllStudents());
        return "teacher/grades/create";
    }

    @PostMapping("/create")
    public String createGrade(@Valid @ModelAttribute("gradeDto") GradeDto gradeDto,
                            BindingResult bindingResult,
                            Model model,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
            model.addAttribute("gradeTypes", GradeType.values());
            model.addAttribute("students", teacherService.getAllStudents());
            return "teacher/grades/create";
        }

        try {
            teacherService.createGrade(gradeDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Grade created successfully!");
            return "redirect:/teacher/grades";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create grade: " + e.getMessage());
            model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
            model.addAttribute("gradeTypes", GradeType.values());
            model.addAttribute("students", teacherService.getAllStudents());
            return "teacher/grades/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditGradeForm(@PathVariable Long id, Model model, Principal principal) {
        try {
            Grade grade = teacherService.getGradeById(id, principal.getName());
            GradeDto gradeDto = teacherService.convertGradeToDto(grade);
            
            model.addAttribute("gradeDto", gradeDto);
            model.addAttribute("gradeId", id);
            model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
            model.addAttribute("gradeTypes", GradeType.values());
            model.addAttribute("students", teacherService.getAllStudents());
            return "teacher/grades/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Grade not found or access denied");
            return "redirect:/teacher/grades";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateGrade(@PathVariable Long id,
                            @Valid @ModelAttribute("gradeDto") GradeDto gradeDto,
                            BindingResult bindingResult,
                            Model model,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("gradeId", id);
            model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
            model.addAttribute("gradeTypes", GradeType.values());
            model.addAttribute("students", teacherService.getAllStudents());
            return "teacher/grades/edit";
        }

        try {
            teacherService.updateGrade(id, gradeDto, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Grade updated successfully!");
            return "redirect:/teacher/grades";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update grade: " + e.getMessage());
            model.addAttribute("gradeId", id);
            model.addAttribute("subjects", teacherService.getSubjectsByTeacher(principal.getName()));
            model.addAttribute("gradeTypes", GradeType.values());
            model.addAttribute("students", teacherService.getAllStudents());
            return "teacher/grades/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteGrade(@PathVariable Long id, 
                            Principal principal, 
                            RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteGrade(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Grade deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete grade: " + e.getMessage());
        }
        return "redirect:/teacher/grades";
    }

    @PostMapping("/{id}/publish")
    public String publishGrade(@PathVariable Long id, 
                             Principal principal, 
                             RedirectAttributes redirectAttributes) {
        try {
            teacherService.publishGrade(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Grade published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish grade: " + e.getMessage());
        }
        return "redirect:/teacher/grades";
    }

    @GetMapping("/bulk-entry")
    public String showBulkGradeEntry(@RequestParam Long subjectId, 
                                   @RequestParam String gradeType,
                                   Model model, 
                                   Principal principal) {
        try {
            List<User> students = teacherService.getStudentsBySubject(subjectId);
            Subject subject = teacherService.getSubjectById(subjectId);
            
            model.addAttribute("students", students);
            model.addAttribute("subject", subject);
            model.addAttribute("gradeType", gradeType);
            return "teacher/grades/bulk-entry";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading bulk grade entry: " + e.getMessage());
            return "redirect:/teacher/grades";
        }
    }

    @PostMapping("/bulk-entry")
    public String processBulkGradeEntry(@RequestParam Long subjectId,
                                      @RequestParam String gradeType,
                                      @RequestParam String semester,
                                      @RequestParam String academicYear,
                                      @RequestParam java.util.Map<String, String> grades,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            teacherService.processBulkGrades(subjectId, gradeType, semester, academicYear, grades, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Bulk grades processed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process bulk grades: " + e.getMessage());
        }
        return "redirect:/teacher/grades";
    }
}
