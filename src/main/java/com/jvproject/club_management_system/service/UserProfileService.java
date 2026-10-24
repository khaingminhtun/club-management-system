package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.profile.UpdateProfileRequest;
import com.jvproject.club_management_system.dto.profile.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getMyProfile();

    UserProfileResponse updateMyProfile(UpdateProfileRequest request);

}
