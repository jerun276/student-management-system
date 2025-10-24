package com.student_management_system.staff.controller;

import com.student_management_system.staff.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import com.student_management_system.staff.dto.RecordPaymentDto;
import com.student_management_system.staff.model.Fee;
import com.student_management_system.staff.model.PaymentMethod;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    private final StaffService staffService;

    public StaffDashboardController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("fees", staffService.getAllFees());
        return "staff/dashboard";
    }

    @GetMapping("/fees/{id}/record-payment")
    public String showRecordPaymentForm(@PathVariable Long id, Model model) {
        Fee fee = staffService.findFeeById(id);
        model.addAttribute("fee", fee);
        model.addAttribute("payment", new RecordPaymentDto());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "staff/record-payment";
    }

    @PostMapping("/fees/{id}/record-payment")
    public String processRecordPayment(@PathVariable Long id,
                                       @Valid @ModelAttribute("payment") RecordPaymentDto paymentDto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            // If validation fails, return to the form with errors
            model.addAttribute("fee", staffService.findFeeById(id));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "staff/record-payment";
        }

        try {
            staffService.recordPayment(id, paymentDto);
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/dashboard";
    }

    @PostMapping("/fees/{id}/send-reminder")
    public String sendFeeReminder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            staffService.sendFeeReminder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reminder sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reminder: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @GetMapping("/fees/assign")
    public String showAssignFeeForm(Model model) {
        model.addAttribute("students", staffService.getAllStudents());
        model.addAttribute("gradeLevels", staffService.getAllGradeLevels());
        return "staff/assign-fee";
    }

    @PostMapping("/fees/assign-single")
    public String assignFeeToStudent(@RequestParam Long studentId,
                                     @RequestParam String title,
                                     @RequestParam java.math.BigDecimal amount,
                                     @RequestParam String dueDate,
                                     RedirectAttributes redirectAttributes) {
        try {
            staffService.assignFeeToStudent(studentId, title, amount, java.time.LocalDate.parse(dueDate));
            redirectAttributes.addFlashAttribute("successMessage", "Fee assigned to student successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign fee: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/fees/assign-multiple")
    public String assignFeeToMultipleStudents(@RequestParam List<Long> studentIds,
                                              @RequestParam String title,
                                              @RequestParam java.math.BigDecimal amount,
                                              @RequestParam String dueDate,
                                              RedirectAttributes redirectAttributes) {
        try {
            staffService.assignFeeToMultipleStudents(studentIds, title, amount, java.time.LocalDate.parse(dueDate));
            redirectAttributes.addFlashAttribute("successMessage", "Fee assigned to " + studentIds.size() + " students successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign fee: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/fees/assign-grade-level")
    public String assignFeeToGradeLevel(@RequestParam Long gradeLevelId,
                                        @RequestParam String title,
                                        @RequestParam java.math.BigDecimal amount,
                                        @RequestParam String dueDate,
                                        RedirectAttributes redirectAttributes) {
        try {
            staffService.assignFeeToGradeLevel(gradeLevelId, title, amount, java.time.LocalDate.parse(dueDate));
            redirectAttributes.addFlashAttribute("successMessage", "Fee assigned to grade level successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign fee: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

}