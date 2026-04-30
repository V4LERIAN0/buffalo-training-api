package com.buffalotraining.wod.dto;

import com.buffalotraining.wod.entity.WodStatus;
import com.buffalotraining.wod.entity.WodType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateWodRequest {

    @Size(max = 120, message = "Title must not exceed 120 characters")
    private String title;

    private String description;

    private LocalDate wodDate;

    private WodType wodType;

    private WodStatus status;

    private String warmUp;

    private String skillOrStrength;

    @Size(max = 255, message = "Video URL must not exceed 255 characters")
    private String videoUrl;

    private String movementNotes;

    /*
     * Optional:
     * If provided, this replaces all existing variants for the WOD.
     */
    @Valid
    private List<CreateWodVariantRequest> variants;
}