package com.jvproject.club_management_system.dto.club;

import com.jvproject.club_management_system.enums.ClubRole;

import java.time.Instant;

public record ClubMemberResponse(
        Long userId,
        String fullName,
        String email,
        ClubRole role,
        Instant joinedAt
) {
}
