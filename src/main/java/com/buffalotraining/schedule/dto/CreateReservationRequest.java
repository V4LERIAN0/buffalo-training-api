package com.buffalotraining.schedule.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReservationRequest {

    @NotNull(message = "Class session ID is required")
    private Long classSessionId;

    @NotNull(message = "Athlete ID is required")
    private Long athleteId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}