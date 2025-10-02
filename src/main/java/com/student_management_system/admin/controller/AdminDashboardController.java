package com.student_management_system.admin.controller;

import com.student_management_system.admin.dto.CreateUserDto;
import com.student_management_system.admin.service.AdminService;
import com.student_management_system.user_management.model.Role;
import com.student_management_system.admin.dto.UserDto;
import com.student_management_system.common.service.AcademicYearService;
import com.student_management_system.common.model.AcademicYear;
import com.student_management_system.user_management.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;
    
    @Autowired
    private AcademicYearService academicYearService;

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
        
        // Add current academic year
        Optional<AcademicYear> currentAcademicYear = academicYearService.getCurrentAcademicYear();
        model.addAttribute("currentAcademicYear", currentAcademicYear.orElse(null));
        
        // Add current user information for UI restrictions
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("currentUsername", authentication.getName());
        
        return "admin/dashboard";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("newUser") CreateUserDto createUserDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        
        // Custom validation for business rules
        validateCreateUser(createUserDto, bindingResult);
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newUser", bindingResult);
            redirectAttributes.addFlashAttribute("newUser", createUserDto);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the validation errors and try again.");
            return "redirect:/admin/dashboard";
        }
        
        try {
            adminService.createUser(createUserDto);
            redirectAttributes.addFlashAttribute("successMessage", 
                "User '" + createUserDto.getUsername() + "' created successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newUser", createUserDto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newUser", createUserDto);
        }
        return "redirect:/admin/dashboard";
    }
    
    private void validateCreateUser(CreateUserDto createUserDto, BindingResult bindingResult) {
        // Check username uniqueness
        if (createUserDto.getUsername() != null && adminService.isUsernameExists(createUserDto.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", 
                "Username '" + createUserDto.getUsername() + "' already exists");
        }
        
        // Check email uniqueness
        if (createUserDto.getEmail() != null && adminService.isEmailExists(createUserDto.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", 
                "Email '" + createUserDto.getEmail() + "' is already registered");
        }
        
        // Check NIC uniqueness
        if (createUserDto.getNic() != null && !createUserDto.getNic().trim().isEmpty()) {
            if (adminService.isNicExists(createUserDto.getNic())) {
                bindingResult.rejectValue("nic", "nic.exists", 
                    "NIC '" + createUserDto.getNic() + "' is already registered");
            }
        }
        
        // Age validation based on date of birth
        if (createUserDto.getDateOfBirth() != null) {
            int age = java.time.Period.between(createUserDto.getDateOfBirth(), java.time.LocalDate.now()).getYears();
            if (age < 5) {
                bindingResult.rejectValue("dateOfBirth", "age.tooYoung", 
                    "User must be at least 5 years old");
            }
            if (age > 100) {
                bindingResult.rejectValue("dateOfBirth", "age.tooOld", 
                    "Please enter a valid date of birth");
            }
        }
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOptional = adminService.getUserById(id);
            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
                return "redirect:/admin/dashboard";
            }
            
            User user = userOptional.get();
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setDateOfBirth(user.getDateOfBirth());
            userDto.setAddress(user.getAddress());
            userDto.setPhoneNumber(user.getPhoneNumber());
            userDto.setNic(user.getNic());
            userDto.setRole(user.getRole());
            userDto.setEnabled(user.isEnabled());
            
            model.addAttribute("userDto", userDto);
            model.addAttribute("allRoles", Role.values());
            
            return "admin/user-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading user: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute("userDto") UserDto userDto,
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
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/" + userDto.getId() + "/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
            return "redirect:/admin/dashboard";
        }

        return "redirect:/admin/dashboard";
    }

    // Process the password reset form submission
    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {

        if (newPassword == null || newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "Password must be at least 8 characters long.");
            return "redirect:/admin/users/" + id + "/edit";
        }

        try {
            adminService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            // Get the user to be deleted
            Optional<User> userToDelete = adminService.getUserById(id);
            
            if (userToDelete.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found!");
                return "redirect:/admin/dashboard";
            }
            
            // Prevent self-deletion
            if (userToDelete.get().getUsername().equals(currentUsername)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own account!");
                return "redirect:/admin/dashboard";
            }
            
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}