package com.student_management_system.principal.controller;

import com.student_management_system.principal.service.PrincipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/principal")
public class PrincipalDashboardController {

    private final PrincipalService principalService;

    public PrincipalDashboardController(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("performanceReport", principalService.getSubjectPerformanceReport());
        return "principal/dashboard";
    }
}