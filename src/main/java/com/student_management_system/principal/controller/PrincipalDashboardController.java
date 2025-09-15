package com.student_management_system.principal.controller;

import com.student_management_system.principal.service.PrincipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}