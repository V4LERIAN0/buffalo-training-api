package com.buffalotraining.membership.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.buffalotraining.membership.entity.BillingCycle;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateMembershipPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 80, message = "Plan name must not exceed 80 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Duration days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Grace period days is required")
    @Min(value = 0, message = "Grace period cannot be negative")
    private Integer gracePeriodDays;
}