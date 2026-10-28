package com.jvproject.club_management_system.dto.user;

import com.jvproject.club_management_system.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull AccountStatus status
) {
}
