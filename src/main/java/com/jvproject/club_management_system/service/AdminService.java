package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.admin.AdminProfileResponse;
import com.jvproject.club_management_system.dto.club.ClubRejectionRequest;
import com.jvproject.club_management_system.dto.club.PendingClubResponse;
import com.jvproject.club_management_system.dto.club.ClubResponse;
import com.jvproject.club_management_system.dto.user.UserResponse;
import com.jvproject.club_management_system.dto.user.UpdateUserStatusRequest;

import java.util.List;


public interface AdminService {

    AdminProfileResponse getMyAdminProfile(String email);

    List<PendingClubResponse> getPendingClubs();

    List<ClubResponse> getClubs();

    ClubResponse getClub(Long clubId);

    List<UserResponse> getUsers();

    UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);

    void approveClub(Long clubId);

    void rejectClub(Long clubId, ClubRejectionRequest request);
}
