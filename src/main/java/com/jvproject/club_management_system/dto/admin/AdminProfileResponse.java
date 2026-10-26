package com.jvproject.club_management_system.dto.admin;

public record AdminProfileResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
