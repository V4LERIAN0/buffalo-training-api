package com.buffalotraining.auth.dto;

import com.buffalotraining.user.dto.UserResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String message;
    private UserResponse user;
}