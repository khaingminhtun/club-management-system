package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.auth.AuthResponse;
import com.jvproject.club_management_system.dto.auth.RegisterRequest;
import com.jvproject.club_management_system.enums.RoleName;
import com.jvproject.club_management_system.model.Role;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.RoleRepository;
import com.jvproject.club_management_system.repository.UserRepository;
import com.jvproject.club_management_system.security.JwtService;
import com.jvproject.club_management_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException(
                        "STUDENT role is not configured"
                ));

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(new HashSet<>(List.of(studentRole)))
                .build();

        User savedUser = userRepository.save(user);

        List<String> authorities = savedUser.getRoles()
                .stream()
                .map(role -> "ROLE_" + role.getName().name())
                .toList();

        String accessToken = jwtService.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                authorities
        );

        String refreshToken = jwtService.generateRefreshToken(
                savedUser.getEmail()
        );

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                RoleName.STUDENT.name(),
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessExpiration()
        );
    }
}
