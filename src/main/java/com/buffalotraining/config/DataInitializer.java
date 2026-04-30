package com.buffalotraining.config;

import com.buffalotraining.user.entity.Role;
import com.buffalotraining.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ADMIN", "Administrator with full access to the system");
        createRoleIfNotExists("COACH", "Coach responsible for classes, WODs, and attendance");
        createRoleIfNotExists("ATHLETE", "Athlete member of Buffalo Training");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();

            roleRepository.save(role);
        }
    }
}