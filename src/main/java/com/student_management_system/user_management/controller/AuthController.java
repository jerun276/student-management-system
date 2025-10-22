package com.student_management_system.user_management.controller;

import com.student_management_system.user_management.dto.UserRegistrationDto;
import com.student_management_system.user_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                authentication.isAuthenticated() && 
                                !authentication.getName().equals("anonymousUser");
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        // If authenticated, determine the appropriate dashboard URL based on user role
        if (isAuthenticated) {
            String dashboardUrl = getDashboardUrlByRole(authentication);
            model.addAttribute("dashboardUrl", dashboardUrl);
        }
        
        return "index";
    }
    
    private String getDashboardUrlByRole(Authentication authentication) {
        // Get user authorities/roles
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        
        switch (role) {
            case "ROLE_ADMIN":
                return "/admin/dashboard";
            case "ROLE_TEACHER":
                return "/teacher/dashboard";
            case "ROLE_STUDENT":
                return "/student/dashboard";
            case "ROLE_PRINCIPAL":
                return "/principal/dashboard";
            case "ROLE_PARENT":
                return "/parent/dashboard";
            case "ROLE_STAFF":
                return "/staff/dashboard";
            default:
                return "/dashboard"; // fallback
        }
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
            // 3. If the username or email already exists, add an error to the model
            model.addAttribute("registrationError", e.getMessage());
            model.addAttribute("user", registrationDto);
            return "auth/register";
        } catch (Exception e) {
            // 4. Handle any other exceptions
            model.addAttribute("registrationError", "An error occurred during registration: " + e.getMessage());
            model.addAttribute("user", registrationDto);
            return "auth/register";
        }

        // 4. If successful, redirect to a success page or the login page
        return "redirect:/login?registrationSuccess";
    }
}