package com.buffalotraining.schedule.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateClassSessionRequest {

    @NotBlank(message = "Class name is required")
    @Size(max = 80, message = "Class name must not exceed 80 characters")
    private String className;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Class date is required")
    private LocalDate classDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Coach ID is required")
    private Long coachId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}