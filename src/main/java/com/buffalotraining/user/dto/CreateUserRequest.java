package com.buffalotraining.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 80, message = "First name must not exceed 80 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 80, message = "Last name must not exceed 80 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Role name is required")
    private String roleName;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Size(max = 255, message = "Profile photo URL must not exceed 255 characters")
    private String profilePhotoUrl;
}