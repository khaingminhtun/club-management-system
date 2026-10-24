package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.profile.UpdateProfileRequest;
import com.jvproject.club_management_system.dto.profile.UserProfileResponse;
import com.jvproject.club_management_system.exception.ResourceNotFoundException;
import com.jvproject.club_management_system.exception.UnauthorizedException;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.model.UserProfile;
import com.jvproject.club_management_system.repository.UserProfileRepository;
import com.jvproject.club_management_system.repository.UserRepository;
import com.jvproject.club_management_system.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getMyProfile() {

        User user = getAuthenticatedUser();

        UserProfile profile = userProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {

        User user = getAuthenticatedUser();

        UserProfile profile = userProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User profile is not found"));

        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName());
        }

        if (request.profileImageUrl() != null) {
            profile.setProfileImageUrl(request.profileImageUrl());
        }

        if (request.bio() != null) {
            profile.setBio(request.bio());
        }

        if (request.department() != null) {
            profile.setDepartment(request.department());
        }

        if (request.academicYear() != null) {
            profile.setAcademicYear(request.academicYear());
        }
        return mapToResponse(profile);
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new UnauthorizedException("Authentication is required");
        }

        return userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found"));
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        String displayName = profile.getDisplayName() != null
                && !profile.getDisplayName().isBlank()
                ? profile.getDisplayName()
                : profile.getUser().getFullName();

        return new UserProfileResponse(
                displayName,
                profile.getProfileImageUrl(),
                profile.getBio(),
                profile.getDepartment(),
                profile.getAcademicYear(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
