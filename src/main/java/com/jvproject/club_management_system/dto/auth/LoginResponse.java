
package com.jvproject.club_management_system.dto.auth;

import java.util.Set;

public record LoginResponse(
        String message,
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            Long id,
            String studentId,
            String fullName,
            String email,
            Set<String> roles
    ) {}
}
