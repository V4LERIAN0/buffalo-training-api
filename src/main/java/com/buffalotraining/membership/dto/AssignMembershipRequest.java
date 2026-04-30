package com.buffalotraining.membership.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignMembershipRequest {

    @NotNull(message = "Athlete ID is required")
    private Long athleteId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}