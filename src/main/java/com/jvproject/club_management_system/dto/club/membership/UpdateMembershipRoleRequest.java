package com.jvproject.club_management_system.dto.club.membership;

import com.jvproject.club_management_system.enums.ClubRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMembershipRoleRequest(
        @NotNull ClubRole role
) {
}
