package com.student_management_system.admin.controller;

import com.student_management_system.admin.dto.UserManagementDto;
import com.student_management_system.admin.service.AdminService;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.user_management.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "username") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir,
                           @RequestParam(required = false) Role roleFilter,
                           @RequestParam(required = false) String searchTerm,
                           Model model) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> userPage;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                userPage = adminService.searchUsers(searchTerm, roleFilter, pageable);
            } else if (roleFilter != null) {
                userPage = adminService.getUsersByRole(roleFilter, pageable);
            } else {
                userPage = adminService.getAllUsers(pageable);
            }
            
            model.addAttribute("userPage", userPage);
            model.addAttribute("roles", Role.values());
            model.addAttribute("currentRoleFilter", roleFilter);
            model.addAttribute("currentSearchTerm", searchTerm);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentSortDir", sortDir);
            
            return "admin/users/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading users: " + e.getMessage());
            return "admin/users/list";
        }
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        UserManagementDto userDto = new UserManagementDto();
        model.addAttribute("userDto", userDto);
        model.addAttribute("roles", Role.values());
        model.addAttribute("parents", adminService.getPotentialParents());
        return "admin/users/create";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userDto") UserManagementDto userDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        // Custom validation for password confirmation
        if (userDto.getPassword() != null && !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("parents", adminService.getPotentialParents());
            return "admin/users/create";
        }

        try {
            adminService.createUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("parents", adminService.getPotentialParents());
            return "admin/users/create";
        }
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        try {
            User user = adminService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/users/view";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        try {
            User user = adminService.getUserById(id);
            UserManagementDto userDto = adminService.convertToDto(user);
            
            model.addAttribute("userDto", userDto);
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("parents", adminService.getPotentialParents());
            return "admin/users/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute("userDto") UserManagementDto userDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        // Password confirmation validation (only if password is being changed)
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("parents", adminService.getPotentialParents());
            return "admin/users/edit";
        }

        try {
            adminService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update user: " + e.getMessage());
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("parents", adminService.getPotentialParents());
            return "admin/users/edit";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deactivateUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to deactivate user: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.activateUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate user: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/change-role")
    public String changeUserRole(@PathVariable Long id, 
                                @RequestParam Role newRole, 
                                RedirectAttributes redirectAttributes) {
        try {
            adminService.changeUserRole(id, newRole);
            redirectAttributes.addFlashAttribute("successMessage", "User role changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change user role: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }

    @PostMapping("/{id}/reset-password")
    public String resetUserPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String newPassword = adminService.resetUserPassword(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Password reset successfully! New password: " + newPassword);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reset password: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }
}
