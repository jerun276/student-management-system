package com.student_management_system.parent.controller;

import com.student_management_system.parent.service.ParentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parent")
public class ParentDashboardController {

    private final ParentService parentService;

    public ParentDashboardController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        model.addAttribute("childrenData", parentService.getChildrenDashboardData());
        return "parent/dashboard";
    }
}