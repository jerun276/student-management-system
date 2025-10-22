package com.student_management_system.staff.controller;

import com.student_management_system.staff.model.Booking;
import com.student_management_system.staff.model.Facility;
import com.student_management_system.staff.service.StaffService;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String createBooking(@RequestParam Long facilityId,
                                @RequestParam String purpose,
                                @RequestParam String startTime,
                                @RequestParam String endTime,
                                RedirectAttributes redirectAttributes) {

        try {
            // Fetch the facility by ID
            Facility facility = staffService.getFacilityById(facilityId);
            if (facility == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Selected facility not found.");
                return "redirect:/staff/facilities";
            }

            // Create booking object and set facility
            Booking booking = new Booking();
            booking.setFacility(facility);
            booking.setPurpose(purpose);
            booking.setStartTime(java.time.LocalDateTime.parse(startTime));
            booking.setEndTime(java.time.LocalDateTime.parse(endTime));

            User currentUser = getCurrentUser();
            staffService.createBooking(booking, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating booking: " + e.getMessage());
        }

        return "redirect:/staff/facilities";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found."));
    }
}