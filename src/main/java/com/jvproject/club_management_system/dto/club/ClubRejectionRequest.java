package com.jvproject.club_management_system.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClubRejectionRequest(

        @NotBlank(message = "Rejection reason is required")
        @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
        String reason

) {
}
