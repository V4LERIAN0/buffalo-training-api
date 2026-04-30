package com.buffalotraining.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class UpdateClassSessionRequest {

    @Size(max = 80, message = "Class name must not exceed 80 characters")
    private String className;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private LocalDate classDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Long coachId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}