package com.jvproject.club_management_system.config;

import com.jvproject.club_management_system.model.Role;
import com.jvproject.club_management_system.enums.RoleName;
import com.jvproject.club_management_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Arrays.stream(RoleName.values())
                .forEach(roleName ->
                        roleRepository.findByName(roleName)
                                .orElseGet(() ->
                                        roleRepository.save(
                                                new Role(roleName)
                                        )
                                )
                );
    }
}