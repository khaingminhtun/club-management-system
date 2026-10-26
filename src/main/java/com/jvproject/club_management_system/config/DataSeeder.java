 package com.jvproject.club_management_system.config;

import com.jvproject.club_management_system.enums.AccountStatus;
import com.jvproject.club_management_system.enums.RoleName;
import com.jvproject.club_management_system.model.Role;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.RoleRepository;
import com.jvproject.club_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean adminBootstrapEnabled;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.full-name:Club System Administrator}")
    private String adminFullName;

    @Value("${app.bootstrap.admin.student-id:}")
    private String adminStudentId;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();

        if (adminBootstrapEnabled) {
            seedAdmin();
        }
    }

    private void seedRoles() {
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

    private void seedAdmin() {
        validateAdminConfiguration();

        String normalizedEmail = adminEmail.trim()
                .toLowerCase(Locale.ROOT);

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ADMIN role was not created."
                        )
                );

        var existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail);

        if (existingUser.isPresent()) {
            boolean alreadyAdmin = existingUser.get().getRoles()
                    .stream()
                    .anyMatch(role -> role.getName() == RoleName.ADMIN);

            if (!alreadyAdmin) {
                throw new IllegalStateException(
                        "The configured admin email already belongs to "
                                + "a non-admin account. Refusing to promote it."
                );
            }

            // Do not reset an existing admin's password on startup.
            return;
        }

        String normalizedStudentId = adminStudentId.trim();

        if (userRepository.existsByStudentId(normalizedStudentId)) {
            throw new IllegalStateException(
                    "The configured admin student ID is already in use."
            );
        }

        User admin = User.builder()
                .studentId(normalizedStudentId)
                .fullName(adminFullName.trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .enabled(true)
                .status(AccountStatus.ACTIVE)
                .emailVerifiedAt(Instant.now())
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
    }

    private void validateAdminConfiguration() {
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_EMAIL must be configured."
            );
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD must be configured."
            );
        }

        if (adminPassword.length() < 3) {
            throw new IllegalStateException(
                    "The bootstrap admin password must be at least "
                            + "3 characters."
            );
        }

        if (adminFullName == null || adminFullName.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_FULL_NAME must be configured."
            );
        }

        if (adminStudentId == null || adminStudentId.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_STUDENT_ID must be configured."
            );
        }
    }
}
