package com.jvproject.club_management_system.dto.auth;

public record RefreshTokenResponse(
        String message,
        String accessToken,
        String tokenType,
        long expiresIn
) {}