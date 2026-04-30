package com.buffalotraining.user.dto;

import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String profilePhotoUrl;
    private UserStatus status;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .status(user.getStatus())
                .roleName(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}