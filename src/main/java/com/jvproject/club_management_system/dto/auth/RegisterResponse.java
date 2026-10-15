package com.jvproject.club_management_system.dto.auth;

public record RegisterResponse(
        String message,
        String email,
        String status
) {}