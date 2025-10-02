package com.student_management_system.admin.controller;

import com.student_management_system.common.model.*;
import com.student_management_system.common.service.EnrollmentService;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeLevelRepository gradeLevelRepository;

    @GetMapping("/enrollments")
    public String showEnrollments(@RequestParam(required = false) Long academicYear,
            @RequestParam(required = false) Long gradeLevel,
            @RequestParam(required = false) String medium,
            @RequestParam(required = false) String status,
            Model model) {
        try {
            // Get current academic year if none specified
            AcademicYear selectedYear;
            if (academicYear != null) {
                Optional<AcademicYear> yearOpt = academicYearService.getAcademicYearById(academicYear);
                selectedYear = yearOpt.orElse(null);
            } else {
                Optional<AcademicYear> currentYear = academicYearService.getCurrentAcademicYear();
                selectedYear = currentYear.orElse(null);
            }

            // Get enrollments based on filters
            List<Enrollment> enrollments;
            if (selectedYear != null) {
                if ("active".equals(status)) {
                    enrollments = enrollmentRepository.findActiveEnrollmentsByAcademicYear(selectedYear);
                } else if ("inactive".equals(status)) {
                    enrollments = enrollmentRepository.findByAcademicYear(selectedYear).stream()
                            .filter(e -> !e.isActive())
                            .toList();
                } else {
                    enrollments = enrollmentRepository.findByAcademicYear(selectedYear);
                }

                // Filter by grade if specified
                if (gradeLevel != null) {
                    enrollments = enrollments.stream()
                            .filter(e -> e.getClassroom().getGradeLevel().getId().equals(gradeLevel))
                            .toList();
                }

                // Filter by medium if specified
                if (medium != null && !medium.isEmpty()) {
                    enrollments = enrollments.stream()
                            .filter(e -> e.getClassroom().getMedium().toString().equals(medium))
                            .toList();
                }
            } else {
                enrollments = List.of();
            }

            // Get all academic years and grade levels for filters
            List<AcademicYear> academicYears = academicYearService.getAllAcademicYears();
            List<GradeLevel> gradeLevels = gradeLevelRepository.findAll();

            // Calculate statistics
            long totalEnrollments = enrollments.size();
            long activeEnrollments = enrollments.stream().mapToLong(e -> e.isActive() ? 1 : 0).sum();
            long inactiveEnrollments = totalEnrollments - activeEnrollments;

            model.addAttribute("enrollments", enrollments);
            model.addAttribute("academicYears", academicYears);
            model.addAttribute("gradeLevels", gradeLevels);
            model.addAttribute("selectedYear", selectedYear);
            model.addAttribute("selectedGradeId", gradeLevel);
            model.addAttribute("selectedMedium", medium);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("totalEnrollments", totalEnrollments);
            model.addAttribute("activeEnrollments", activeEnrollments);
            model.addAttribute("inactiveEnrollments", inactiveEnrollments);

            return "admin/enrollments";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading enrollments: " + e.getMessage());
            return "admin/enrollments";
        }
    }

    @PostMapping("/enrollments/{id}/reactivate")
    public String reactivateEnrollment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(id);
            if (enrollmentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Enrollment not found");
                return "redirect:/admin/enrollments";
            }

            Enrollment enrollment = enrollmentOpt.get();

            // Check if student is already actively enrolled in this academic year
            Optional<Enrollment> activeEnrollment = enrollmentRepository
                    .findActiveEnrollmentByStudentAndAcademicYear(enrollment.getStudent(),
                            enrollment.getAcademicYear());

            if (activeEnrollment.isPresent() && !activeEnrollment.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Student is already actively enrolled in another classroom for this academic year");
                return "redirect:/admin/enrollments";
            }

            // Reactivate enrollment
            enrollment.setActive(true);
            enrollment.setWithdrawalDate(null);
            enrollment.setRemarks("Reactivated by administrator");
            enrollmentRepository.save(enrollment);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Enrollment reactivated successfully for " + enrollment.getStudent().getFirstName() +
                            " " + enrollment.getStudent().getLastName());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error reactivating enrollment: " + e.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    @PostMapping("/enrollments/{id}/withdraw")
    public String withdrawEnrollment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(id);
            if (enrollmentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Enrollment not found");
                return "redirect:/admin/enrollments";
            }

            Enrollment enrollment = enrollmentOpt.get();

            // Check if enrollment is already inactive
            if (!enrollment.isActive()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Student is already withdrawn");
                return "redirect:/admin/enrollments";
            }

            // Withdraw the student using enrollment service
            enrollmentService.withdrawStudent(enrollment.getStudent(), enrollment.getAcademicYear(),
                    "Withdrawn via enrollment management");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Student " + enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName() +
                            " withdrawn from " + enrollment.getClassroom().getFullName() + " successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error withdrawing student: " + e.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    @GetMapping("/enrollments/{id}/transfer-info")
    @ResponseBody
    public Map<String, Object> getTransferInfo(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(id);
            if (enrollmentOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Enrollment not found");
                return response;
            }

            Enrollment enrollment = enrollmentOpt.get();

            // Check if enrollment is active
            if (!enrollment.isActive()) {
                response.put("success", false);
                response.put("message", "Cannot transfer inactive enrollment");
                return response;
            }

            response.put("success", true);
            response.put("classroomId", enrollment.getClassroom().getId());
            response.put("studentId", enrollment.getStudent().getId());
            response.put("studentName",
                    enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
            response.put("classroomName", enrollment.getClassroom().getFullName());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting transfer information: " + e.getMessage());
        }

        return response;
    }
}
