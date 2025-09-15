package com.student_management_system.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/principal/dashboard")
    public String principalDashboard() {
        return "principal/dashboard";
    }
}
