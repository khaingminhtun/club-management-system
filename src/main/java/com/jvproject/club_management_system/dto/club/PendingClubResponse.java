package com.jvproject.club_management_system.dto.club;

import java.time.Instant;

public record PendingClubResponse(
        Long id,
        String name,
        String slug,
        String description,
        String logoUrl,
        String coverImageUrl,
        String status,
        Instant createdAt,
        Long founderId,
        String founderName,
        String founderEmail
) {
}