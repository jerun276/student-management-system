package com.student_management_system.staff.dto;

import com.student_management_system.staff.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecordPaymentDto {

    @NotNull(message = "Payment amount cannot be empty.")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero.")
    private BigDecimal amount;

    @NotNull(message = "Payment method must be selected.")
    private PaymentMethod paymentMethod;
}