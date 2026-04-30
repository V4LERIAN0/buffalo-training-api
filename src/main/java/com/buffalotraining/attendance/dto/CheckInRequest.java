package com.buffalotraining.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckInRequest {

    @NotNull(message = "Reservation ID is required")
    private Long reservationId;

    @NotNull(message = "Athlete ID is required")
    private Long athleteId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}