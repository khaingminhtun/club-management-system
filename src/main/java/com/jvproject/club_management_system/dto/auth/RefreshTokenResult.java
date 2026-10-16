package com.jvproject.club_management_system.dto.auth;

public record RefreshTokenResult(
        RefreshTokenResponse response,
        String refreshToken
) {}