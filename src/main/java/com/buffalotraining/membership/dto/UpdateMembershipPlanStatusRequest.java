package com.buffalotraining.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMembershipPlanStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;
}