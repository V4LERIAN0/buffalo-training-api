package com.buffalotraining.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateAttendanceRequest {

    @NotNull(message = "Validated by user ID is required")
    private Long validatedByUserId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}