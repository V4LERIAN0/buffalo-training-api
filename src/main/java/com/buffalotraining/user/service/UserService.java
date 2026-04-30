package com.buffalotraining.user.service;

import com.buffalotraining.user.dto.CreateUserRequest;
import com.buffalotraining.user.dto.UpdateUserRequest;
import com.buffalotraining.user.dto.UpdateUserStatusRequest;
import com.buffalotraining.user.dto.UserResponse;
import com.buffalotraining.user.entity.Role;
import com.buffalotraining.user.entity.User;
import com.buffalotraining.user.entity.UserStatus;
import com.buffalotraining.user.repository.RoleRepository;
import com.buffalotraining.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedRoleName = request.getRoleName().trim().toUpperCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        Role role = roleRepository.findByName(normalizedRoleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .gender(request.getGender())
                .profilePhotoUrl(request.getProfilePhotoUrl())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = findUserEntityById(id);
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserEntityById(id);

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();

            boolean emailBelongsToAnotherUser = userRepository.findByEmail(normalizedEmail)
                    .map(existingUser -> !existingUser.getId().equals(id))
                    .orElse(false);

            if (emailBelongsToAnotherUser) {
                throw new IllegalArgumentException("A user with this email already exists");
            }

            user.setEmail(normalizedEmail);
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }

        User updatedUser = userRepository.save(user);

        return UserResponse.fromEntity(updatedUser);
    }

    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = findUserEntityById(id);
        user.setStatus(request.getStatus());

        User updatedUser = userRepository.save(user);

        return UserResponse.fromEntity(updatedUser);
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}