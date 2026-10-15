package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.auth.RegisterRequest;
import com.jvproject.club_management_system.dto.auth.RegisterResponse;
import com.jvproject.club_management_system.enums.AccountStatus;
import com.jvproject.club_management_system.enums.EnrollmentStatus;
import com.jvproject.club_management_system.exception.ConflictException;
import com.jvproject.club_management_system.exception.RegistrationRejectedException;
import com.jvproject.club_management_system.model.StudentRegistry;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.RoleRepository;
import com.jvproject.club_management_system.repository.StudentRegistryRepository;
import com.jvproject.club_management_system.repository.UserRepository;
import com.jvproject.club_management_system.security.JwtService;
import com.jvproject.club_management_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRegistryRepository studentRegistryRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String studentId = request.studentId().trim();

        String fullName = request.fullName().trim();

        // 1. Check whether the account already exists.
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(
                    "An account with this email already exists."
            );
        }

        if (userRepository.existsByStudentId(studentId)) {
            throw new ConflictException(
                    "This student ID is already registered."
            );
        }

        // 2. Find a matching trusted registry record.
        StudentRegistry registry = studentRegistryRepository
                .findByStudentIdAndUniversityEmailIgnoreCase(
                        studentId,
                        email
                )
                .orElseThrow(() ->
                        new RegistrationRejectedException(
                                "Unable to verify student details."
                        )
                );

        // 3. Check enrollment eligibility.
        if (registry.getEnrollmentStatus() != EnrollmentStatus.ACTIVE) {
            throw new RegistrationRejectedException(
                    "Unable to verify student details."
            );
        }

        // 4. Confirm the submitted name against trusted registry data.
        if (!registry.getFullName().trim()
                .equalsIgnoreCase(fullName)) {
            throw new RegistrationRejectedException(
                    "Unable to verify student details."
            );
        }

        // 5. Create the pending UCMS account.
        User user = User.builder()
                .fullName(fullName)
                .studentId(studentId)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .enabled(true)
                .status(AccountStatus.PENDING_EMAIL)
                .studentVerifiedAt(Instant.now())
                .build();

        userRepository.save(user);

        // 6. OTP creation and email delivery will be added next.
        return new RegisterResponse(
                "Registration accepted. Verify your email to activate your account.",
                email,
                AccountStatus.PENDING_EMAIL.name()
        );
    }
}
