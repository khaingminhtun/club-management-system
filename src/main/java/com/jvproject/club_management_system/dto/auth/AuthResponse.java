package com.jvproject.club_management_system.dto.auth;

public record AuthResponse(
        Long userId,
        String fullName,
        String email,
        String role,
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn
) {
}