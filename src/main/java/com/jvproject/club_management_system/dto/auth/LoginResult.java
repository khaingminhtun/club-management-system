package com.jvproject.club_management_system.dto.auth;

public record LoginResult(
        LoginResponse response,
        String refreshToken
) {}