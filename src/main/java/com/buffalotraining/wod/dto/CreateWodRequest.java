package com.buffalotraining.wod.dto;

import com.buffalotraining.wod.entity.WodStatus;
import com.buffalotraining.wod.entity.WodType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateWodRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must not exceed 120 characters")
    private String title;

    private String description;

    @NotNull(message = "WOD date is required")
    private LocalDate wodDate;

    @NotNull(message = "WOD type is required")
    private WodType wodType;

    private WodStatus status;

    private String warmUp;

    private String skillOrStrength;

    @Size(max = 255, message = "Video URL must not exceed 255 characters")
    private String videoUrl;

    private String movementNotes;

    @NotNull(message = "Created by user ID is required")
    private Long createdByUserId;

    @NotEmpty(message = "At least one WOD variant is required")
    @Valid
    private List<CreateWodVariantRequest> variants;
}