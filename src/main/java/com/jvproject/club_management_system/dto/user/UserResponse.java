package com.jvproject.club_management_system.dto.user;

import com.jvproject.club_management_system.enums.AccountStatus;

import java.time.Instant;

public record UserResponse(
        Long id,
        String studentId,
        String fullName,
        String email,
        boolean enabled,
        AccountStatus status,
        Instant emailVerifiedAt,
        Instant studentVerifiedAt,
        Instant createdAt
) {
}
