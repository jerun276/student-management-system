package com.student_management_system.principal.controller;

import com.student_management_system.principal.service.PrincipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/principal")
public class PrincipalDashboardController {

    private final PrincipalService principalService;

    public PrincipalDashboardController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("announcements", principalService.getAllAnnouncements());
        model.addAttribute("performanceReport", principalService.getSubjectPerformanceReport());
        model.addAttribute("teacherPerformanceReport", principalService.getTeacherPerformanceReport());
        model.addAttribute("pendingRequests", principalService.getPendingBudgetRequests());
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
}