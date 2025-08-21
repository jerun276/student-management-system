package com.student_management_system.user_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login"; // Renders templates/auth/login.html
    }

    @GetMapping("/")
    public String home() {
        return "index"; // Renders templates/index.html
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register"; // Renders templates/auth/register.html
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password"; // Renders templates/auth/forgot-password.html
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "auth/reset-password"; // Renders templates/auth/reset-password.html
    }
}