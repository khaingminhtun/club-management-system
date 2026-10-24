package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.auth.*;
import com.jvproject.club_management_system.enums.AccountStatus;
import com.jvproject.club_management_system.enums.EnrollmentStatus;
import com.jvproject.club_management_system.enums.RoleName;
import com.jvproject.club_management_system.exception.*;
import com.jvproject.club_management_system.integration.email.OtpService;
import com.jvproject.club_management_system.model.*;
import com.jvproject.club_management_system.repository.*;
import com.jvproject.club_management_system.security.JwtService;
import com.jvproject.club_management_system.service.AuthService;
import com.jvproject.club_management_system.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRegistryRepository studentRegistryRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserProfileRepository userProfileRepository;



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

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName(user.getFullName())
                .profileImageUrl(null)
                .bio(null)
                .department(null)
                .academicYear(null)
                .build();

        userProfileRepository.save(profile);

        otpService.sendRegistrationOtp(
                request.email(),
                request.fullName()
        );

        // 6. OTP creation and email delivery will be added next.
        return new RegisterResponse(
                "Registration accepted. Verify your email to activate your account.",
                email,
                AccountStatus.PENDING_EMAIL.name()
        );
    }

    @Override
    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pending registration was not found"
                ));

        if (user.getEmailVerifiedAt() != null) {
            return new VerifyEmailResponse(
                    "Email is already verified",
                    true
            );
        }



        if (!otpService.verifyRegistrationOtp(email, request.otp())) {
            log.warn("invalid veficiton");
            throw new InvalidOtpException(
                    "Invalid or expired verification code"
            );
        }

        user.setEmailVerifiedAt(Instant.now());

        // Assign the default STUDENT role
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "STUDENT role is not configured"
                ));

        user.getRoles().add(studentRole);

        if (user.getStudentVerifiedAt() != null) {
            user.setStatus(AccountStatus.ACTIVE);
        } else {
            user.setStatus(AccountStatus.PENDING_STUDENT_VERIFICATION);
        }

        userRepository.save(user);

        return new VerifyEmailResponse(
                "Email verified successfully",
                true
        );
    }

    @Override
    @Transactional
    public LoginResult login(
            LoginRequest request,
            String deviceName,
            String ipAddress
    ) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        if (user.getEmailVerifiedAt() == null) {
            throw new AccountNotActiveException(
                    "Please verify your email before logging in."
            );
        }

        if (user.getStatus() != AccountStatus.ACTIVE || !user.isEnabled()) {
            throw new AccountNotActiveException(
                    "Your account is not active."
            );
        }

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                List.copyOf(roles)
        );

        String refreshToken = jwtService.generateRefreshToken(
                user.getEmail()
        );

        Instant now = Instant.now();

        UserSession session = UserSession.builder()
                .user(user)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .refreshTokenHash(TokenHashUtil.sha256(refreshToken))
                .expiresAt(now.plus(jwtService.getRefreshTokenLifetime()))
                .lastUsedAt(now)
                .build();

        userSessionRepository.save(session);

        LoginResponse response = new LoginResponse(
                "Login successful",
                accessToken,
                "Bearer",
                jwtService.getAccessTokenLifetime().toSeconds(),
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getStudentId(),
                        user.getFullName(),
                        user.getEmail(),
                        roles
                )
        );

        return new LoginResult(response, refreshToken);
    }


    @Override
    @Transactional
    public RefreshTokenResult refresh(String presentedRefreshToken) {
        if (presentedRefreshToken == null || presentedRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token is missing"
            );
        }

        // 1. Validate JWT signature, expiration, and token type.
        final String email;

        try {
            email = jwtService.extractEmail(presentedRefreshToken);
        } catch (io.jsonwebtoken.JwtException
                 | IllegalArgumentException ex) {
            throw new InvalidRefreshTokenException(
                    "Invalid or expired refresh token"
            );
        }

        if (!jwtService.isRefreshTokenValid(presentedRefreshToken, email)) {
            throw new InvalidRefreshTokenException(
                    "Invalid or expired refresh token"
            );
        }

        // 2. Find the corresponding server-side session.
        String presentedHash = TokenHashUtil.sha256(presentedRefreshToken);

        UserSession session = userSessionRepository
                .findByRefreshTokenHash(presentedHash)
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        "Refresh session not found"
                ));

        Instant now = Instant.now();

        // 3. Check session validity.
        if (session.isRevoked() || session.isExpired()) {
            throw new InvalidRefreshTokenException(
                    "Refresh session has expired or been revoked"
            );
        }

        User user = session.getUser();

        if (!user.isEnabled()
                || user.getStatus() != AccountStatus.ACTIVE
                || user.getEmailVerifiedAt() == null) {
            throw new AccountNotActiveException(
                    "Your account is not active"
            );
        }

        // 4. Generate a new access token using current roles.
        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .toList();

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                roles
        );

        // 5. Rotate the refresh token.
        String newRefreshToken = jwtService.generateRefreshToken(
                user.getEmail()
        );

        session.setRefreshTokenHash(
                TokenHashUtil.sha256(newRefreshToken)
        );
        session.setExpiresAt(
                now.plus(jwtService.getRefreshTokenLifetime())
        );
        session.setLastUsedAt(now);

        userSessionRepository.save(session);

        // 6. Return access token in JSON and refresh token internally.
        RefreshTokenResponse response = new RefreshTokenResponse(
                "Token refreshed successfully",
                accessToken,
                "Bearer",
                jwtService.getAccessTokenLifetime().toSeconds()
        );

        return new RefreshTokenResult(response, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // No cookie: the user is already logged out.
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenHash = TokenHashUtil.sha256(refreshToken);

        userSessionRepository.findByRefreshTokenHash(tokenHash)
                .ifPresent(session -> {
                    // Do not overwrite the original revocation time.
                    if (!session.isRevoked()) {
                        session.setRevokedAt(Instant.now());
                        userSessionRepository.save(session);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ResendOtpResponse resendOtp(ResendOtpRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RegistrationRejectedException(
                        "Unable to resend verification code"
                ));

        // A verified user should not need another registration OTP.
        if (user.getEmailVerifiedAt() != null) {
            throw new RegistrationRejectedException(
                    "Unable to resend verification code"
            );
        }

        // Only pending registrations may request another OTP.
        if (user.getStatus() != AccountStatus.PENDING_EMAIL
                || !user.isEnabled()) {
            throw new RegistrationRejectedException(
                    "Unable to resend verification code"
            );
        }

        otpService.resendRegistrationOtp(
                email,
                user.getFullName()
        );

        return new ResendOtpResponse(
                "If the registration is eligible, a verification code will be sent."
        );
    }



}
