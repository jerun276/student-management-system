package com.student_management_system.staff.controller;

import com.student_management_system.staff.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.student_management_system.staff.dto.RecordPaymentDto;
import com.student_management_system.staff.model.Fee;
import com.student_management_system.staff.model.PaymentMethod;
import com.student_management_system.staff.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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


}