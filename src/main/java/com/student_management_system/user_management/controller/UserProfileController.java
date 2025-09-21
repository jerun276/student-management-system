package com.student_management_system.user_management.controller;

import com.student_management_system.user_management.service.UserService;
import com.student_management_system.user_management.dto.EditProfileDto;
import com.student_management_system.user_management.dto.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfilePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        UserProfileDto userProfile = userService.getUserProfile(principal.getName());
        model.addAttribute("user", userProfile);

        return "user/profile";
    }

    @GetMapping("/edit")
    public String showEditProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        EditProfileDto editProfileDto = userService.getEditProfileDto(principal.getName());
        model.addAttribute("editProfileDto", editProfileDto);

        return "user/profile-edit";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("editProfileDto") EditProfileDto editProfileDto,
                                BindingResult bindingResult,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "user/profile-edit";
        }

        try {
            userService.updateUserProfile(principal.getName(), editProfileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/profile";
    }
}
