package com.buffalotraining.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(max = 80, message = "First name must not exceed 80 characters")
    private String firstName;

    @Size(max = 80, message = "Last name must not exceed 80 characters")
    private String lastName;

    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Size(max = 255, message = "Profile photo URL must not exceed 255 characters")
    private String profilePhotoUrl;
}