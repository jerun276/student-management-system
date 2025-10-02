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
    private EnrollmentRepository enrollmentRepository;
    
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
        long totalRegisteredStudents = userRepository.countByRole(Role.ROLE_STUDENT);
        long totalTeachers = userRepository.countByRole(Role.ROLE_TEACHER);
        
        // Calculate total enrolled students (students actually enrolled in classrooms)
        long totalEnrolledStudents = 0;
        for (Classroom classroom : classrooms) {
            totalEnrolledStudents += enrollmentService.getCurrentEnrollmentCount(classroom);
        }
        
        // Calculate classroom counts per grade level
        Map<Long, Long> classroomCounts = new HashMap<>();
        for (GradeLevel grade : gradeLevels) {
            long count = classrooms.stream()
                .filter(c -> c.getGradeLevel().getId().equals(grade.getId()))
                .count();
            classroomCounts.put(grade.getId(), count);
        }
        
        // Calculate enrollment counts per classroom
        Map<Long, Long> enrollmentCounts = new HashMap<>();
        for (Classroom classroom : classrooms) {
            Long enrollmentCount = enrollmentService.getCurrentEnrollmentCount(classroom);
            enrollmentCounts.put(classroom.getId(), enrollmentCount);
        }
        
        // Add to model
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("gradeLevels", gradeLevels);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("classroomCounts", classroomCounts);
        model.addAttribute("enrollmentCounts", enrollmentCounts);
        model.addAttribute("currentYear", currentYear.orElse(null));
        model.addAttribute("totalStudents", totalEnrolledStudents);
        model.addAttribute("totalRegisteredStudents", totalRegisteredStudents);
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

    @GetMapping("/classroom/{id}/assign-teacher")
    public String showAssignTeacherForm(@PathVariable Long id, Model model) {
        // Find classroom
        Optional<Classroom> classroomOpt = classroomRepository.findById(id);
        if (classroomOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Classroom not found");
            return "redirect:/admin/academic-structure";
        }
        
        Classroom classroom = classroomOpt.get();
        
        // Get all teachers
        List<User> allTeachers = userRepository.findByRole(Role.ROLE_TEACHER);
        
        // For now, just show all teachers to test
        model.addAttribute("classroom", classroom);
        model.addAttribute("availableTeachers", allTeachers);
        
        return "admin/assign-teacher";
    }

    @PostMapping("/classroom/{id}/assign-teacher")
    public String assignTeacher(@PathVariable Long id, 
                              @RequestParam(required = false) Long teacherId,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<Classroom> classroom = classroomRepository.findById(id);
            if (classroom.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Classroom not found");
                return "redirect:/admin/academic-structure";
            }
            
            Classroom classroomEntity = classroom.get();
            
            if (teacherId == null) {
                // Remove current teacher assignment
                classroomEntity.setClassTeacher(null);
                classroomRepository.save(classroomEntity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Teacher removed from " + classroomEntity.getFullName() + " successfully!");
            } else {
                Optional<User> teacher = userRepository.findById(teacherId);
                if (teacher.isEmpty() || teacher.get().getRole() != Role.ROLE_TEACHER) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Invalid teacher selected");
                    return getRedirectUrl(request, id);
                }
                
                // First, remove this teacher from any other classroom assignments
                // to prevent the duplicate result error
                List<Classroom> allClassrooms = classroomRepository.findAll();
                for (Classroom c : allClassrooms) {
                    if (c.getClassTeacher() != null && 
                        c.getClassTeacher().getId().equals(teacher.get().getId()) && 
                        !c.getId().equals(id)) {
                        c.setClassTeacher(null);
                        classroomRepository.save(c);
                    }
                }
                
                // Now assign the teacher to the new classroom
                classroomEntity.setClassTeacher(teacher.get());
                classroomRepository.save(classroomEntity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Teacher " + teacher.get().getFirstName() + " " + teacher.get().getLastName() + 
                    " assigned to " + classroomEntity.getFullName() + " successfully!");
            }
            
            return getRedirectUrl(request, id);
            
        } catch (Exception e) {
            System.out.println("Error assigning teacher: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error assigning teacher: " + e.getMessage());
            return "redirect:/admin/academic-structure";
        }
    }
    
    private String getRedirectUrl(HttpServletRequest request, Long classroomId) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.contains("/admin/classrooms?gradeId=")) {
                // Extract gradeId from referer
                String gradeId = referer.substring(referer.indexOf("gradeId=") + 8);
                return "redirect:/admin/classrooms?gradeId=" + gradeId;
            } else if (referer.contains("/admin/classroom/" + classroomId + "/students")) {
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
        }
        return "redirect:/admin/academic-structure";
    }

    // ===== EDIT FUNCTIONALITY =====
    
    @GetMapping("/academic-year/{id}/edit")
    public String editAcademicYear(@PathVariable Long id, Model model) {
        try {
            Optional<AcademicYear> academicYear = academicYearRepository.findById(id);
            if (academicYear.isEmpty()) {
                model.addAttribute("errorMessage", "Academic year not found");
                return "redirect:/admin/academic-structure";
            }
            
            // Get statistics for this academic year
            List<Classroom> classrooms = classroomRepository.findByAcademicYear(academicYear.get());
            int totalClassrooms = classrooms.size();
            
            // Count total students across all classrooms for this academic year
            int totalStudents = 0;
            for (Classroom classroom : classrooms) {
                totalStudents += enrollmentService.getCurrentEnrollmentCount(classroom).intValue();
            }
            
            model.addAttribute("academicYear", academicYear.get());
            model.addAttribute("totalClassrooms", totalClassrooms);
            model.addAttribute("totalStudents", totalStudents);
            
            return "admin/edit-academic-year";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading academic year: " + e.getMessage());
            return "redirect:/admin/academic-structure";
        }
    }
    
    @PostMapping("/academic-year/{id}/update")
    public String updateAcademicYear(@PathVariable Long id,
                                   @RequestParam String name,
                                   @RequestParam java.time.LocalDate startDate,
                                   @RequestParam java.time.LocalDate endDate,
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<AcademicYear> academicYearOpt = academicYearRepository.findById(id);
            if (academicYearOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Academic year not found");
                return "redirect:/admin/academic-structure";
            }
            
            AcademicYear academicYear = academicYearOpt.get();
            academicYear.setName(name);
            academicYear.setStartDate(startDate);
            academicYear.setEndDate(endDate);
            
            academicYearRepository.save(academicYear);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Academic year '" + name + "' updated successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating academic year: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }
    
    @GetMapping("/grade-level/{id}/edit")
    public String editGradeLevel(@PathVariable Long id, Model model) {
        try {
            Optional<GradeLevel> gradeLevel = gradeLevelRepository.findById(id);
            if (gradeLevel.isEmpty()) {
                model.addAttribute("errorMessage", "Grade level not found");
                return "redirect:/admin/academic-structure";
            }
            
            // Get statistics for this grade level
            List<Classroom> classrooms = classroomRepository.findByGradeLevel(gradeLevel.get());
            int totalClassrooms = classrooms.size();
            
            // Count total students across all classrooms for this grade level
            int totalStudents = 0;
            for (Classroom classroom : classrooms) {
                totalStudents += enrollmentService.getCurrentEnrollmentCount(classroom).intValue();
            }
            
            model.addAttribute("gradeLevel", gradeLevel.get());
            model.addAttribute("totalClassrooms", totalClassrooms);
            model.addAttribute("totalStudents", totalStudents);
            
            return "admin/edit-grade-level";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading grade level: " + e.getMessage());
            return "redirect:/admin/academic-structure";
        }
    }
    
    @PostMapping("/grade-level/{id}/update")
    public String updateGradeLevel(@PathVariable Long id,
                                 @RequestParam Integer level,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<GradeLevel> gradeLevelOpt = gradeLevelRepository.findById(id);
            if (gradeLevelOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Grade level not found");
                return "redirect:/admin/academic-structure";
            }
            
            GradeLevel gradeLevel = gradeLevelOpt.get();
            gradeLevel.setLevel(level);
            gradeLevel.setName(name);
            gradeLevel.setDescription(description);
            
            gradeLevelRepository.save(gradeLevel);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Grade level '" + name + "' updated successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating grade level: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }
    
    @GetMapping("/classroom/{id}/edit")
    public String editClassroom(@PathVariable Long id, Model model) {
        try {
            Optional<Classroom> classroom = classroomRepository.findById(id);
            if (classroom.isEmpty()) {
                model.addAttribute("errorMessage", "Classroom not found");
                return "redirect:/admin/academic-structure";
            }
            
            // Get data for dropdowns
            List<AcademicYear> academicYears = academicYearRepository.findAll();
            List<GradeLevel> gradeLevels = gradeLevelRepository.findAll();
            
            // Get current enrollment count
            Long currentEnrollment = enrollmentService.getCurrentEnrollmentCount(classroom.get());
            int availableSpots = classroom.get().getMaxStudents() - currentEnrollment.intValue();
            
            model.addAttribute("classroom", classroom.get());
            model.addAttribute("academicYears", academicYears);
            model.addAttribute("gradeLevels", gradeLevels);
            model.addAttribute("mediumValues", Medium.values());
            model.addAttribute("currentEnrollment", currentEnrollment);
            model.addAttribute("availableSpots", availableSpots);
            
            return "admin/edit-classroom";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading classroom: " + e.getMessage());
            return "redirect:/admin/academic-structure";
        }
    }
    
    @PostMapping("/classroom/{id}/update")
    public String updateClassroom(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam Long gradeLevelId,
                                @RequestParam Long academicYearId,
                                @RequestParam Medium medium,
                                @RequestParam Integer maxStudents,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Classroom> classroomOpt = classroomRepository.findById(id);
            Optional<GradeLevel> gradeLevel = gradeLevelRepository.findById(gradeLevelId);
            Optional<AcademicYear> academicYear = academicYearRepository.findById(academicYearId);
            
            if (classroomOpt.isEmpty() || gradeLevel.isEmpty() || academicYear.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Invalid classroom, grade level, or academic year selected.");
                return "redirect:/admin/academic-structure";
            }
            
            Classroom classroom = classroomOpt.get();
            
            // Validate max students is not less than current enrollment
            Long currentEnrollment = enrollmentService.getCurrentEnrollmentCount(classroom);
            if (maxStudents < currentEnrollment) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Maximum students (" + maxStudents + ") cannot be less than current enrollment (" + currentEnrollment + ")");
                return "redirect:/admin/classroom/" + id + "/edit";
            }
            
            classroom.setName(name);
            classroom.setGradeLevel(gradeLevel.get());
            classroom.setAcademicYear(academicYear.get());
            classroom.setMedium(medium);
            classroom.setMaxStudents(maxStudents);
            
            classroomRepository.save(classroom);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Classroom '" + classroom.getFullName() + "' updated successfully!");
                
            // Smart redirect back to where user came from
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/admin/classrooms?gradeId=")) {
                return "redirect:/admin/classrooms?gradeId=" + gradeLevelId;
            } else if (referer != null && referer.contains("/admin/classroom/" + id + "/students")) {
                return "redirect:/admin/classroom/" + id + "/students";
            }
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating classroom: " + e.getMessage());
        }
        return "redirect:/admin/academic-structure";
    }

    // ===== STUDENT ENROLLMENT FUNCTIONALITY =====
    
    @GetMapping("/classroom/{id}/enroll-student")
    public String showEnrollStudentForm(@PathVariable Long id, Model model) {
        try {
            Optional<Classroom> classroom = classroomRepository.findById(id);
            if (classroom.isEmpty()) {
                model.addAttribute("errorMessage", "Classroom not found");
                return "redirect:/admin/academic-structure";
            }
            
            // Get current enrollment count
            Long currentEnrollment = enrollmentService.getCurrentEnrollmentCount(classroom.get());
            
            // Check if classroom is at capacity
            if (currentEnrollment >= classroom.get().getMaxStudents()) {
                model.addAttribute("errorMessage", "Classroom is at maximum capacity");
                return "redirect:/admin/classroom/" + id + "/students";
            }
            
            // Get all students who are not enrolled in any classroom for this academic year
            List<User> allStudents = userRepository.findByRole(Role.ROLE_STUDENT);
            List<User> availableStudents = new java.util.ArrayList<>();
            
            for (User student : allStudents) {
                // Check if student is already enrolled in any classroom for this academic year
                boolean isEnrolled = enrollmentService.isStudentEnrolledInAcademicYear(student, classroom.get().getAcademicYear());
                if (!isEnrolled) {
                    availableStudents.add(student);
                }
            }
            
            model.addAttribute("classroom", classroom.get());
            model.addAttribute("availableStudents", availableStudents);
            model.addAttribute("currentEnrollment", currentEnrollment);
            model.addAttribute("availableSpots", classroom.get().getMaxStudents() - currentEnrollment.intValue());
            
            return "admin/enroll-student";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading enrollment form: " + e.getMessage());
            return "redirect:/admin/classroom/" + id + "/students";
        }
    }
    
    @PostMapping("/classroom/{id}/enroll-student")
    public String enrollStudent(@PathVariable Long id,
                               @RequestParam Long studentId,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Classroom> classroom = classroomRepository.findById(id);
            Optional<User> student = userRepository.findById(studentId);
            
            if (classroom.isEmpty() || student.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid classroom or student selected");
                return "redirect:/admin/classroom/" + id + "/students";
            }
            
            if (student.get().getRole() != Role.ROLE_STUDENT) {
                redirectAttributes.addFlashAttribute("errorMessage", "Selected user is not a student");
                return "redirect:/admin/classroom/" + id + "/students";
            }
            
            // Check capacity
            Long currentEnrollment = enrollmentService.getCurrentEnrollmentCount(classroom.get());
            if (currentEnrollment >= classroom.get().getMaxStudents()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Classroom is at maximum capacity");
                return "redirect:/admin/classroom/" + id + "/students";
            }
            
            // Check if student is already enrolled in this academic year
            boolean isAlreadyEnrolled = enrollmentService.isStudentEnrolledInAcademicYear(student.get(), classroom.get().getAcademicYear());
            if (isAlreadyEnrolled) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Student " + student.get().getFirstName() + " " + student.get().getLastName() + 
                    " is already enrolled in a classroom for this academic year");
                return "redirect:/admin/classroom/" + id + "/students";
            }
            
            // Enroll the student
            enrollmentService.enrollStudent(student.get(), classroom.get());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Student " + student.get().getFirstName() + " " + student.get().getLastName() + 
                " enrolled in " + classroom.get().getFullName() + " successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error enrolling student: " + e.getMessage());
        }
        
        return "redirect:/admin/classroom/" + id + "/students";
    }

    // ===== STUDENT TRANSFER FUNCTIONALITY =====
    
    @GetMapping("/classroom/{classroomId}/transfer-student/{studentId}")
    public String showTransferStudentForm(@PathVariable Long classroomId, 
                                        @PathVariable Long studentId, 
                                        Model model) {
        try {
            Optional<Classroom> currentClassroom = classroomRepository.findById(classroomId);
            Optional<User> student = userRepository.findById(studentId);
            
            if (currentClassroom.isEmpty() || student.isEmpty()) {
                model.addAttribute("errorMessage", "Classroom or student not found");
                return "redirect:/admin/academic-structure";
            }
            
            // Verify student is actually enrolled in this classroom
            boolean isEnrolled = enrollmentService.isStudentEnrolledInClassroom(student.get(), currentClassroom.get());
            if (!isEnrolled) {
                model.addAttribute("errorMessage", "Student is not enrolled in this classroom");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Get all other classrooms in the same academic year (excluding current classroom)
            List<Classroom> availableClassrooms = classroomRepository
                .findByAcademicYear(currentClassroom.get().getAcademicYear())
                .stream()
                .filter(c -> !c.getId().equals(classroomId))
                .filter(c -> enrollmentService.getAvailableSpots(c) > 0) // Only classrooms with available spots
                .collect(java.util.stream.Collectors.toList());
            
            // Get current enrollment info
            Long currentEnrollment = enrollmentService.getCurrentEnrollmentCount(currentClassroom.get());
            
            // Create enrollment counts map for available classrooms
            Map<Long, Long> enrollmentCounts = new HashMap<>();
            Map<Long, Integer> availableSpots = new HashMap<>();
            for (Classroom classroom : availableClassrooms) {
                Long enrollment = enrollmentService.getCurrentEnrollmentCount(classroom);
                enrollmentCounts.put(classroom.getId(), enrollment);
                availableSpots.put(classroom.getId(), enrollmentService.getAvailableSpots(classroom));
            }
            
            model.addAttribute("student", student.get());
            model.addAttribute("currentClassroom", currentClassroom.get());
            model.addAttribute("availableClassrooms", availableClassrooms);
            model.addAttribute("currentEnrollment", currentEnrollment);
            model.addAttribute("enrollmentCounts", enrollmentCounts);
            model.addAttribute("availableSpots", availableSpots);
            
            return "admin/transfer-student";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading transfer form: " + e.getMessage());
            return "redirect:/admin/classroom/" + classroomId + "/students";
        }
    }
    
    @PostMapping("/classroom/{classroomId}/transfer-student/{studentId}")
    public String transferStudent(@PathVariable Long classroomId,
                                @PathVariable Long studentId,
                                @RequestParam Long newClassroomId,
                                @RequestParam(required = false) String remarks,
                                @RequestParam(required = false) String returnTo,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Classroom> currentClassroom = classroomRepository.findById(classroomId);
            Optional<Classroom> newClassroom = classroomRepository.findById(newClassroomId);
            Optional<User> student = userRepository.findById(studentId);
            
            if (currentClassroom.isEmpty() || newClassroom.isEmpty() || student.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid classroom or student selected");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Validation checks
            if (student.get().getRole() != Role.ROLE_STUDENT) {
                redirectAttributes.addFlashAttribute("errorMessage", "Selected user is not a student");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Check if trying to transfer to same classroom
            if (currentClassroom.get().getId().equals(newClassroom.get().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot transfer student to the same classroom");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Check if both classrooms are in the same academic year
            if (!currentClassroom.get().getAcademicYear().getId().equals(newClassroom.get().getAcademicYear().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot transfer between different academic years");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Verify student is enrolled in current classroom
            boolean isEnrolled = enrollmentService.isStudentEnrolledInClassroom(student.get(), currentClassroom.get());
            if (!isEnrolled) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student is not enrolled in the current classroom");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Check new classroom capacity
            Long newClassroomEnrollment = enrollmentService.getCurrentEnrollmentCount(newClassroom.get());
            if (newClassroomEnrollment >= newClassroom.get().getMaxStudents()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Target classroom is at maximum capacity");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Perform the transfer
            enrollmentService.transferStudent(student.get(), newClassroom.get(), currentClassroom.get().getAcademicYear());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Student " + student.get().getFirstName() + " " + student.get().getLastName() + 
                " transferred from " + currentClassroom.get().getFullName() + 
                " to " + newClassroom.get().getFullName() + " successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error transferring student: " + e.getMessage());
        }
        
        // Determine where to redirect based on returnTo parameter
        if ("enrollments".equals(returnTo)) {
            return "redirect:/admin/enrollments";
        } else {
            return "redirect:/admin/classroom/" + classroomId + "/students";
        }
    }

    // ===== STUDENT WITHDRAWAL FUNCTIONALITY =====
    
    @PostMapping("/classroom/{classroomId}/withdraw-student/{studentId}")
    public String withdrawStudent(@PathVariable Long classroomId,
                                @PathVariable Long studentId,
                                @RequestParam(required = false) String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Classroom> classroom = classroomRepository.findById(classroomId);
            Optional<User> student = userRepository.findById(studentId);
            
            if (classroom.isEmpty() || student.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid classroom or student selected");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Verify student is enrolled in this classroom
            boolean isEnrolled = enrollmentService.isStudentEnrolledInClassroom(student.get(), classroom.get());
            if (!isEnrolled) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student is not enrolled in this classroom");
                return "redirect:/admin/classroom/" + classroomId + "/students";
            }
            
            // Withdraw the student
            String withdrawalRemarks = (remarks != null && !remarks.trim().isEmpty()) ? remarks : "Withdrawn by administrator";
            enrollmentService.withdrawStudent(student.get(), classroom.get().getAcademicYear(), withdrawalRemarks);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Student " + student.get().getFirstName() + " " + student.get().getLastName() + 
                " withdrawn from " + classroom.get().getFullName() + " successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error withdrawing student: " + e.getMessage());
        }
        
        return "redirect:/admin/classroom/" + classroomId + "/students";
    }
    
    // ===== STUDENT PROFILE FUNCTIONALITY =====
    
    @GetMapping("/student/{studentId}/profile")
    public String viewStudentProfile(@PathVariable Long studentId, 
                                   @RequestParam(required = false) String returnTo,
                                   Model model) {
        try {
            Optional<User> student = userRepository.findById(studentId);
            if (student.isEmpty()) {
                model.addAttribute("errorMessage", "Student not found");
                return "redirect:/admin/dashboard";
            }
            
            // Get student's enrollment history
            List<Enrollment> enrollmentHistory = enrollmentRepository.findByStudent(student.get());
            
            // Get current active enrollment
            Optional<Enrollment> currentEnrollment = enrollmentHistory.stream()
                .filter(Enrollment::isActive)
                .findFirst();
            
            // Calculate enrollment statistics
            long totalEnrollments = enrollmentHistory.size();
            long activeEnrollments = enrollmentHistory.stream().mapToLong(e -> e.isActive() ? 1 : 0).sum();
            long withdrawnEnrollments = totalEnrollments - activeEnrollments;
            
            model.addAttribute("student", student.get());
            model.addAttribute("enrollmentHistory", enrollmentHistory);
            model.addAttribute("currentEnrollment", currentEnrollment.orElse(null));
            model.addAttribute("totalEnrollments", totalEnrollments);
            model.addAttribute("activeEnrollments", activeEnrollments);
            model.addAttribute("withdrawnEnrollments", withdrawnEnrollments);
            model.addAttribute("returnTo", returnTo);
            
            return "admin/student-profile";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading student profile: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
}
