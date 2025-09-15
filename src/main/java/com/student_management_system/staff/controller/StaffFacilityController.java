package com.student_management_system.staff.controller;

import com.student_management_system.staff.model.Booking;
import com.student_management_system.staff.model.Facility;
import com.student_management_system.staff.service.StaffService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/facilities")
public class StaffFacilityController {

    private final StaffService staffService;
    private final UserRepository userRepository;

    public StaffFacilityController(StaffService staffService, UserRepository userRepository) {
        this.staffService = staffService;
        this.userRepository = userRepository;
    }

    // List all facilities and existing bookings
    @GetMapping
    public String showBookingDashboard(Model model) {
        model.addAttribute("facilities", staffService.getAllFacilities());
        model.addAttribute("bookings", staffService.getAllBookings());
        // Add an empty object for the "New Booking" form
        if (!model.containsAttribute("newBooking")) {
            model.addAttribute("newBooking", new Booking());
        }
        return "staff/facilities-dashboard";
    }

    // Handle the creation of a new facility
    @PostMapping("/create")
    public String createFacility(@RequestParam String name, RedirectAttributes redirectAttributes) {
        Facility facility = new Facility();
        facility.setName(name); // In a real app, you'd have a full form and DTO
        staffService.createFacility(facility);
        redirectAttributes.addFlashAttribute("successMessage", "New facility created successfully!");
        return "redirect:/staff/facilities";
    }

    // Handle the creation of a new booking
    @PostMapping("/bookings/create")
    public String createBooking(@Valid @ModelAttribute("newBooking") Booking booking,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newBooking", bindingResult);
            redirectAttributes.addFlashAttribute("newBooking", booking);
            return "redirect:/staff/facilities";
        }

        try {
            User currentUser = getCurrentUser();
            staffService.createBooking(booking, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newBooking", booking); // Send back the failed booking data
        }

        return "redirect:/staff/facilities";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found."));
    }
}