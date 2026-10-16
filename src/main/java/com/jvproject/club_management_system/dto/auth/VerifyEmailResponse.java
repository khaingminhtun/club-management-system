
package com.jvproject.club_management_system.dto.auth;

public record VerifyEmailResponse(
        String message,
        boolean verified
) {
}
