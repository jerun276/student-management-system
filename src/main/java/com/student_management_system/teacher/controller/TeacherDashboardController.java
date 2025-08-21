package com.student_management_system.teacher.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {

    @GetMapping("/dashboard")
    public String getDashboard() {
        return "teacher/dashboard"; // Renders templates/teacher/dashboard.html
    }

}