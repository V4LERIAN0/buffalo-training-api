package com.buffalotraining.leaderboard.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateWodScoreRequest {

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