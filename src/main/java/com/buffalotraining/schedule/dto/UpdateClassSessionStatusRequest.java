package com.buffalotraining.schedule.dto;

import com.buffalotraining.schedule.entity.ClassSessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClassSessionStatusRequest {

    @NotNull(message = "Class session status is required")
    private ClassSessionStatus status;
}