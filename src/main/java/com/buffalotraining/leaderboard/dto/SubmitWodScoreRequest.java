package com.buffalotraining.leaderboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubmitWodScoreRequest {

    @NotNull(message = "WOD variant ID is required")
    private Long wodVariantId;

    @NotNull(message = "Athlete ID is required")
    private Long athleteId;

    @NotBlank(message = "Score text is required")
    @Size(max = 120, message = "Score text must not exceed 120 characters")
    private String scoreText;

    private Integer timeSeconds;

    private Integer reps;

    private Integer rounds;

    private BigDecimal weight;

    private BigDecimal distance;

    private Integer calories;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}