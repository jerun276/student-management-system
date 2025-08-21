package com.student_management_system.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/parent/dashboard")
    public String parentDashboard() {
        return "parent/dashboard";
    }

    @GetMapping("/principal/dashboard")
    public String principalDashboard() {
        return "principal/dashboard";
    }

    @GetMapping("/staff/dashboard")
    public String staffDashboard() {
        return "staff/dashboard";
    }
}
