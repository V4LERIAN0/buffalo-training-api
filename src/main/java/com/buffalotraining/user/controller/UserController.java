package com.buffalotraining.user.controller;

import com.buffalotraining.user.dto.CreateUserRequest;
import com.buffalotraining.user.dto.UpdateUserRequest;
import com.buffalotraining.user.dto.UpdateUserStatusRequest;
import com.buffalotraining.user.dto.UserResponse;
import com.buffalotraining.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return userService.updateUserStatus(id, request);
    }
}