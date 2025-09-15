package com.student_management_system.user_management.controller;

import com.student_management_system.user_management.dto.UserRegistrationDto;
import com.student_management_system.user_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.student_management_system.common.service.PublicService;


@Controller
public class AuthController {

    private final UserService userService;
    private final PublicService publicService;

    // Inject the UserService
    public AuthController(UserService userService, PublicService publicService) {
        this.userService = userService;
        this.publicService = publicService;
    }

    @GetMapping("/")
    public String home(Model model) {
        publicService.getLatestAnnouncement().ifPresent(announcement ->
                model.addAttribute("latestAnnouncement", announcement));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // Method to show the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Add an empty DTO to the model to bind form data
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    // Method to process the registration form
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            Model model) {

        // 1. Check for validation errors
        if (bindingResult.hasErrors()) {
            // If there are errors, return to the form to display them
            return "auth/register";
        }

        try {
            // 2. Try to register the user
            userService.registerNewStudent(registrationDto);
        } catch (IllegalStateException e) {
            // 3. If the username already exists, add an error to the model
            model.addAttribute("registrationError", e.getMessage());
            return "auth/register";
        }

        // 4. If successful, redirect to a success page or the login page
        return "redirect:/login?registrationSuccess";
    }
}