package com.jvproject.club_management_system.dto.club.membership;

import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.MembershipStatus;

import java.time.Instant;

public record ClubMembershipResponse(
        Long id,
        Long clubId,
        Long userId,
        String fullName,
        String email,
        ClubRole role,
        MembershipStatus status,
        Instant requestedAt,
        Instant joinedAt
) {
}
