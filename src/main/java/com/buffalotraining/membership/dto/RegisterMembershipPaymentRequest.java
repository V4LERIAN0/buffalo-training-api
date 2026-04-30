package com.buffalotraining.membership.dto;

import com.buffalotraining.membership.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RegisterMembershipPaymentRequest {

    @NotNull(message = "Membership ID is required")
    private Long membershipId;

    @NotNull(message = "Registered by user ID is required")
    private Long registeredByUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 80, message = "Reference code must not exceed 80 characters")
    private String referenceCode;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}