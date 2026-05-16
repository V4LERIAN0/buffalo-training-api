package com.buffalotraining.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordUpdateResponse {

    private String message;
    private Long userId;
    private String email;
}