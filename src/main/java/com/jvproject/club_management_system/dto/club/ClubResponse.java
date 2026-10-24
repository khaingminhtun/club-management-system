package com.jvproject.club_management_system.dto.club;

import com.jvproject.club_management_system.enums.ClubStatus;

import java.time.Instant;

public record ClubResponse(
        Long id,
        String name,
        String slug,
        String description,
        String logoUrl,
        String coverImageUrl,
        ClubStatus status,
        Long createdById,
        String createdByName,
        long memberCount,
        Instant createdAt,
        Instant updatedAt
) {
}