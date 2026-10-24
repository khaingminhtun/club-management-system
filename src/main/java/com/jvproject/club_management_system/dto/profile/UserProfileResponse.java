package com.jvproject.club_management_system.dto.profile;


import java.time.Instant;

public record UserProfileResponse(
        String displayName,
        String profileImageUrl,
        String bio,
        String department,
        String academicYear,
        Instant createdAt,
        Instant updatedAt
) {
}
