package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.admin.AdminProfileResponse;
import com.jvproject.club_management_system.dto.club.ClubRejectionRequest;
import com.jvproject.club_management_system.dto.club.PendingClubResponse;

import java.util.List;


public interface AdminService {

    AdminProfileResponse getMyAdminProfile(String email);

    List<PendingClubResponse> getPendingClubs();

    void approveClub(Long clubId);

    void rejectClub(Long clubId, ClubRejectionRequest request);
}