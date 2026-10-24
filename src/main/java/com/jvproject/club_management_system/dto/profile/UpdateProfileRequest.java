package com.jvproject.club_management_system.dto.profile;



import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UpdateProfileRequest(

        @Size(max = 100, message = "Display name must not exceed 100 characters")
        String displayName,

        @Size(max = 2048, message = "Profile image URL must not exceed 2048 characters")
        @URL(message = "Profile image URL must be valid")
        String profileImageUrl,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        @Size(max = 150, message = "Department must not exceed 150 characters")
        String department,

        @Size(max = 50, message = "Academic year must not exceed 50 characters")
        String academicYear

) {
}
