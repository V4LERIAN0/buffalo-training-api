package com.buffalotraining.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualAttendanceRequest {

    @NotNull(message = "Class session ID is required")
    private Long classSessionId;

    @NotNull(message = "Athlete ID is required")
    private Long athleteId;

    @NotNull(message = "Registered by user ID is required")
    private Long registeredByUserId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}