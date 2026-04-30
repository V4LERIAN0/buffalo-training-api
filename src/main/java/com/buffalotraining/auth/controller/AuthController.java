package com.buffalotraining.auth.controller;

import com.buffalotraining.auth.dto.LoginRequest;
import com.buffalotraining.auth.dto.LoginResponse;
import com.buffalotraining.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}