package com.buffalotraining.user.dto;

import com.buffalotraining.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}