package com.student_management_system.admin.controller;

import com.student_management_system.common.model.*;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.service.EnrollmentService;
import com.student_management_system.common.repository.*;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AcademicStructureController {

    @Autowired
    private AcademicYearService academicYearService;
    
    @Autowired
    private AcademicYearRepository academicYearRepository;
    
    @Autowired
    private GradeLevelRepository gradeLevelRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/academic-structure")
    public String showAcademicStructure(Model model) {
        // Get all academic data
        List<AcademicYear> academicYears = academicYearRepository.findAll();
        List<GradeLevel> gradeLevels = gradeLevelRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        
        // Get current academic year
        Optional<AcademicYear> currentYear = academicYearService.getCurrentAcademicYear();
        
        // Get statistics
        long totalStudents = userRepository.countByRole(Role.ROLE_STUDENT);
        long totalTeachers = userRepository.countByRole(Role.ROLE_TEACHER);
        
        // Calculate classroom counts per grade level
        Map<Long, Long> classroomCounts = new HashMap<>();
        for (GradeLevel grade : gradeLevels) {
            long count = classrooms.stream()
                .filter(c -> c.getGradeLevel().getId().equals(grade.getId()))
                .count();
            classroomCounts.put(grade.getId(), count);
        }
        
        // Add to model
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("gradeLevels", gradeLevels);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("classroomCounts", classroomCounts);
        model.addAttribute("currentYear", currentYear.orElse(null));
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("mediumValues", Medium.values());
        
        // Add empty objects for forms
        model.addAttribute("newAcademicYear", new AcademicYear());
        model.addAttribute("newGradeLevel", new GradeLevel());
        model.addAttribute("newClassroom", new Classroom());
        
        return "admin/academic-structure";
    }

    @PostMapping("/academic-year/create")
    public String createAcademicYear(@ModelAttribute AcademicYear academicYear,
                                   RedirectAttributes redirectAttributes) {
        try {
            academicYearService.createAcademicYear(
                academicYear.getName(), 
                academicYear.getStartDate(), 
                academicYear.getEndDate()
            );
            redirectAttributes.addFlashAttribute("successMessage", 
                "Academic year '" + academicYear.getName() + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating academic year: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    @PostMapping("/academic-year/{id}/activate")
    public String activateAcademicYear(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            academicYearService.setActiveAcademicYear(id);
            redirectAttributes.addFlashAttribute("successMessage", "Academic year activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error activating academic year: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    @PostMapping("/grade-level/create")
    public String createGradeLevel(@ModelAttribute GradeLevel gradeLevel,
                                 RedirectAttributes redirectAttributes) {
        try {
            gradeLevelRepository.save(gradeLevel);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Grade level '" + gradeLevel.getName() + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating grade level: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    @PostMapping("/classroom/create")
    public String createClassroom(@RequestParam String name,
                                @RequestParam Long gradeLevelId,
                                @RequestParam Long academicYearId,
                                @RequestParam Medium medium,
                                @RequestParam Integer maxStudents,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<GradeLevel> gradeLevel = gradeLevelRepository.findById(gradeLevelId);
            Optional<AcademicYear> academicYear = academicYearRepository.findById(academicYearId);
            
            if (gradeLevel.isEmpty() || academicYear.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Invalid grade level or academic year selected.");
                return "redirect:/admin/academic-structure";
            }
            
            Classroom classroom = new Classroom();
            classroom.setName(name);
            classroom.setGradeLevel(gradeLevel.get());
            classroom.setAcademicYear(academicYear.get());
            classroom.setMedium(medium);
            classroom.setMaxStudents(maxStudents);
            
            classroomRepository.save(classroom);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Classroom '" + classroom.getFullName() + "' created successfully!");
                
            // Redirect back to grade-specific view if we came from there
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/admin/classrooms?gradeId=")) {
                return "redirect:/admin/classrooms?gradeId=" + gradeLevelId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating classroom: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    @PostMapping("/academic-year/{id}/delete")
    public String deleteAcademicYear(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<AcademicYear> academicYear = academicYearRepository.findById(id);
            if (academicYear.isPresent()) {
                if (academicYear.get().isActive()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Cannot delete the active academic year. Please activate another year first.");
                } else {
                    academicYearRepository.deleteById(id);
                    redirectAttributes.addFlashAttribute("successMessage", "Academic year deleted successfully!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting academic year: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    @GetMapping("/classroom/{id}/students")
    public String viewClassroomStudents(@PathVariable Long id, Model model) {
        try {
            Optional<Classroom> classroom = classroomRepository.findById(id);
            if (classroom.isPresent()) {
                List<User> students = enrollmentService.getStudentsInClassroom(classroom.get());
                Long enrollmentCount = enrollmentService.getCurrentEnrollmentCount(classroom.get());
                Integer availableSpots = enrollmentService.getAvailableSpots(classroom.get());
                
                model.addAttribute("classroom", classroom.get());
                model.addAttribute("students", students);
                model.addAttribute("enrollmentCount", enrollmentCount);
                model.addAttribute("availableSpots", availableSpots);
                
                return "admin/classroom-students";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading classroom students: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    @GetMapping("/classrooms")
    public String viewClassroomsByGrade(@RequestParam(required = false) Long gradeId, Model model) {
        try {
            List<Classroom> classrooms;
            String pageTitle;
            
            if (gradeId != null) {
                Optional<GradeLevel> gradeLevel = gradeLevelRepository.findById(gradeId);
                if (gradeLevel.isPresent()) {
                    classrooms = classroomRepository.findByGradeLevel(gradeLevel.get());
                    pageTitle = "Classrooms - " + gradeLevel.get().getName();
                    model.addAttribute("gradeLevel", gradeLevel.get());
                } else {
                    classrooms = classroomRepository.findAll();
                    pageTitle = "All Classrooms";
                }
            } else {
                classrooms = classroomRepository.findAll();
                pageTitle = "All Classrooms";
            }
            
            // Get enrollment counts for each classroom
            Map<Long, Long> enrollmentCounts = new HashMap<>();
            Map<Long, Integer> availableSpots = new HashMap<>();
            
            for (Classroom classroom : classrooms) {
                Long count = enrollmentService.getCurrentEnrollmentCount(classroom);
                Integer spots = enrollmentService.getAvailableSpots(classroom);
                enrollmentCounts.put(classroom.getId(), count);
                availableSpots.put(classroom.getId(), spots);
            }
            
            // Get data for forms
            List<AcademicYear> academicYears = academicYearRepository.findAll();
            List<GradeLevel> gradeLevels = gradeLevelRepository.findAll();
            
            model.addAttribute("classrooms", classrooms);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("enrollmentCounts", enrollmentCounts);
            model.addAttribute("availableSpots", availableSpots);
            model.addAttribute("academicYears", academicYears);
            model.addAttribute("gradeLevels", gradeLevels);
            model.addAttribute("mediumValues", Medium.values());
            
            return "admin/classrooms-by-grade";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading classrooms: " + e.getMessage());
            return "redirect:/admin/academic-structure";
        }
    }
}
