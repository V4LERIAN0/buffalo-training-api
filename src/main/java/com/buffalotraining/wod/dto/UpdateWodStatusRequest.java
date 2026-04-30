package com.buffalotraining.wod.dto;

import com.buffalotraining.wod.entity.WodStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWodStatusRequest {

    @NotNull(message = "WOD status is required")
    private WodStatus status;
}