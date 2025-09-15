package com.student_management_system.staff.controller;

import com.student_management_system.staff.model.Event;
import com.student_management_system.staff.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/events")
public class StaffEventController {

    private final StaffService staffService;

    public StaffEventController(StaffService staffService) {
        this.staffService = staffService;
    }

    // List all events
    @GetMapping
    public String listEvents(Model model) {
        model.addAttribute("events", staffService.getAllEvents());
        return "staff/events-list";
    }

    // Show the form to create a new event
    @GetMapping("/new")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "staff/event-form";
    }

    // Handle the creation of a new event
    @PostMapping("/new")
    public String createEvent(@ModelAttribute Event event, RedirectAttributes redirectAttributes) {
        staffService.createEvent(event);
        redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        return "redirect:/staff/events";
    }
}