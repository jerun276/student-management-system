package com.student_management_system.admin.controller;

import com.student_management_system.admin.dto.CreateUserDto;
import com.student_management_system.admin.service.AdminService;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.admin.dto.UserDto;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        // Add an empty DTO for the "Create User" form modal
        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new CreateUserDto());
        }
        // Pass all Role enum values to the template
        model.addAttribute("allRoles", Role.values());
        return "admin/dashboard";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("newUser") CreateUserDto createUserDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // If there are errors, pass the DTO and errors back to the dashboard
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newUser", bindingResult);
            redirectAttributes.addFlashAttribute("newUser", createUserDto);
            return "redirect:/admin/dashboard";
        }

        try {
            adminService.createUser(createUserDto);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newUser", createUserDto);
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        UserDto userDto = adminService.findUserById(id);
        model.addAttribute("user", userDto);
        model.addAttribute("allRoles", Role.values());
        return "admin/user-edit";
    }

    @PostMapping("/users/update")
    public String updateUser(@Valid @ModelAttribute("user") UserDto userDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", Role.values());
            return "admin/user-edit";
        }

        try {
            adminService.updateUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        } catch (IllegalStateException e) {
            // This will now catch the "Username already taken" error
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Redirect back to the edit page to show the error
            return "redirect:/admin/users/" + userDto.getId() + "/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
            return "redirect:/admin/dashboard";
        }

        return "redirect:/admin/dashboard";
    }
}