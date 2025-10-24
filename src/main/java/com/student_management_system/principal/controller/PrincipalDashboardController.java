package com.student_management_system.principal.controller;

import com.student_management_system.principal.service.PrincipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/principal")
public class PrincipalDashboardController {

    private final PrincipalService principalService;

    public PrincipalDashboardController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Add stats data
        model.addAttribute("totalStudents", principalService.getTotalStudents());
        model.addAttribute("totalTeachers", principalService.getTotalTeachers());
        model.addAttribute("totalSubjects", principalService.getTotalSubjects());
        model.addAttribute("currentAcademicYear", principalService.getCurrentAcademicYear());
        
        // Add reports and announcements
        model.addAttribute("announcements", principalService.getAllAnnouncements());
        model.addAttribute("latestAnnouncement", principalService.getLatestAnnouncement());
        model.addAttribute("performanceReport", principalService.getSubjectPerformanceReport());
        model.addAttribute("teacherPerformanceReport", principalService.getTeacherPerformanceReport());
        model.addAttribute("pendingRequests", principalService.getPendingBudgetRequests());
        
        // Add master timetable and user details
        model.addAttribute("masterTimetable", principalService.getMasterTimetable());
        model.addAttribute("allUsers", principalService.getAllUsers());
        
        return "principal/dashboard";
    }

    // Process the new announcement form
    @PostMapping("/announcements/create")
    public String createAnnouncement(@RequestParam String title,
                                     @RequestParam String content,
                                     RedirectAttributes redirectAttributes) {
        try {
            principalService.createAnnouncement(title, content);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish announcement.");
        }
        return "redirect:/principal/dashboard";
    }
    
    // Delete announcement
    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            principalService.deleteAnnouncement(id);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete announcement.");
        }
        return "redirect:/principal/dashboard";
    }

    @PostMapping("/budget/{id}/approve")
    public String approveBudgetRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        principalService.approveBudgetRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Request approved!");
        return "redirect:/principal/dashboard";
    }

    @PostMapping("/budget/{id}/reject")
    public String rejectBudgetRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        principalService.rejectBudgetRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Request rejected.");
        return "redirect:/principal/dashboard";
    }
    
    @GetMapping("/master-timetable")
    public String viewMasterTimetable(Model model) {
        model.addAttribute("masterTimetable", principalService.getMasterTimetable());
        model.addAttribute("timeSlots", principalService.getTimeSlots());
        model.addAttribute("weekDays", principalService.getWeekDays());
        model.addAttribute("timetableGrid", principalService.getTimetableGrid());
        return "principal/master-timetable";
    }
    
    @GetMapping("/announcements")
    public String viewAnnouncements(Model model) {
        model.addAttribute("announcements", principalService.getAllAnnouncements());
        return "principal/announcements";
    }
    
    @GetMapping("/reports")
    public String viewReports(Model model) {
        // Add all report data
        model.addAttribute("performanceReport", principalService.getSubjectPerformanceReport());
        model.addAttribute("teacherPerformanceReport", principalService.getTeacherPerformanceReport());
        model.addAttribute("totalStudents", principalService.getTotalStudents());
        model.addAttribute("totalTeachers", principalService.getTotalTeachers());
        model.addAttribute("totalSubjects", principalService.getTotalSubjects());
        model.addAttribute("currentAcademicYear", principalService.getCurrentAcademicYear());
        
        return "principal/reports";
    }
    
    @GetMapping("/budget/{id}")
    public String viewBudgetRequest(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var budgetRequest = principalService.getBudgetRequestById(id);
            if (budgetRequest.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Budget request not found.");
                return "redirect:/principal/dashboard";
            }
            model.addAttribute("budgetRequest", budgetRequest.get());
            return "principal/budget-request-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading budget request details.");
            return "redirect:/principal/dashboard";
        }
    }
    
    @GetMapping("/users")
    public String viewUsers(Model model) {
        model.addAttribute("users", principalService.getAllUsersExceptAdmin());
        return "principal/users";
    }
    
    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var user = principalService.getUserById(id);
            if (user.isEmpty() || user.get().getRole().toString().equals("ROLE_ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found or access denied.");
                return "redirect:/principal/users";
            }
            model.addAttribute("user", user.get());
            return "principal/user-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading user details.");
            return "redirect:/principal/users";
        }
    }
    
    // Export reports as CSV
    @GetMapping("/reports/export/csv")
    public ResponseEntity<String> exportReportsAsCSV() {
        try {
            String csvContent = principalService.exportReportsAsCSV();
            String filename = "reports_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(csvContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error exporting CSV: " + e.getMessage());
        }
    }
    
    // Export reports as JSON
    @GetMapping("/reports/export/json")
    public ResponseEntity<String> exportReportsAsJSON() {
        try {
            String jsonContent = principalService.exportReportsAsJSON();
            String filename = "reports_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".json";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
                    .body(jsonContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error exporting JSON: " + e.getMessage());
        }
    }
    
    // Export reports as PDF
    @GetMapping("/reports/export/pdf")
    public ResponseEntity<byte[]> exportReportsAsPDF() {
        try {
            byte[] pdfContent = principalService.exportReportsAsPDF();
            String filename = "reports_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Export users data as CSV
    @GetMapping("/users/export/csv")
    public ResponseEntity<String> exportUsersAsCSV() {
        try {
            String csvContent = principalService.exportUsersAsCSV();
            String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .body(csvContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error exporting CSV: " + e.getMessage());
        }
    }
    
    // Export users data as JSON
    @GetMapping("/users/export/json")
    public ResponseEntity<String> exportUsersAsJSON() {
        try {
            String jsonContent = principalService.exportUsersAsJSON();
            String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".json";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8")
                    .body(jsonContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error exporting JSON: " + e.getMessage());
        }
    }
    
    // Export users data as PDF
    @GetMapping("/users/export/pdf")
    public ResponseEntity<byte[]> exportUsersAsPDF() {
        try {
            byte[] pdfContent = principalService.exportUsersAsPDF();
            String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // View all classrooms with teacher and student details
    @GetMapping("/classrooms")
    public String viewClassrooms(Model model) {
        model.addAttribute("classrooms", principalService.getClassroomsWithDetails());
        return "principal/classrooms";
    }
    
    // View grades for a specific classroom
    @GetMapping("/classrooms/{id}/grades")
    public String viewClassroomGrades(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var classroomDetails = principalService.getClassroomsWithDetails().stream()
                    .filter(c -> c.get("id").equals(id))
                    .findFirst();
            
            if (classroomDetails.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Classroom not found.");
                return "redirect:/principal/classrooms";
            }
            
            model.addAttribute("classroomInfo", classroomDetails.get());
            model.addAttribute("grades", principalService.getGradesForClassroom(id));
            return "principal/classroom-grades";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading classroom grades.");
            return "redirect:/principal/classrooms";
        }
    }
}