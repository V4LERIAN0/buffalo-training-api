package com.buffalotraining.membership.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.buffalotraining.membership.entity.BillingCycle;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateMembershipPlanRequest {

    @Size(max = 80, message = "Plan name must not exceed 80 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @Min(value = 0, message = "Grace period cannot be negative")
    private Integer gracePeriodDays;

    private BillingCycle billingCycle;
}