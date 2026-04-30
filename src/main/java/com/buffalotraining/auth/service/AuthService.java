package com.buffalotraining.auth.service;

import com.buffalotraining.auth.dto.LoginRequest;
import com.buffalotraining.auth.dto.LoginResponse;
import com.buffalotraining.user.dto.UserResponse;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User account is not active");
        }

        return LoginResponse.builder()
                .message("Login successful")
                .user(UserResponse.fromEntity(user))
                .build();
    }
}