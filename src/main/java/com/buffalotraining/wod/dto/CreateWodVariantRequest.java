package com.buffalotraining.wod.dto;

import com.buffalotraining.wod.entity.ExecutionLevel;
import com.buffalotraining.wod.entity.GenderCategory;
import com.buffalotraining.wod.entity.ScoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWodVariantRequest {

    @NotNull(message = "Gender category is required")
    private GenderCategory genderCategory;

    @NotNull(message = "Execution level is required")
    private ExecutionLevel executionLevel;

    @NotNull(message = "Score type is required")
    private ScoreType scoreType;

    @NotBlank(message = "Main workout is required")
    private String mainWorkout;

    private String notes;
}