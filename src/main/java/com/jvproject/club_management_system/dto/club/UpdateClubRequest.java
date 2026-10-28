package com.jvproject.club_management_system.dto.club;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateClubRequest(
        @Pattern(regexp = ".*\\S.*", message = "Club name must not be blank")
        @Size(max = 120, message = "Club name must not exceed 120 characters")
        String name,

        @Pattern(regexp = ".*\\S.*", message = "Club description must not be blank")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        String logoUrl,

        @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
        String coverImageUrl
) {
}
