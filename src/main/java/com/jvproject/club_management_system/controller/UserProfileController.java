package com.jvproject.club_management_system.controller;

import com.jvproject.club_management_system.dto.profile.UpdateProfileRequest;
import com.jvproject.club_management_system.dto.profile.UserProfileResponse;
import com.jvproject.club_management_system.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {

        UserProfileResponse response = userProfileService.getMyProfile();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse response = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(response);
    }
}
