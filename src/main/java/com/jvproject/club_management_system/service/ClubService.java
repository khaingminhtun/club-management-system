package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.club.ClubResponse;
import com.jvproject.club_management_system.dto.club.CreateClubRequest;

import java.util.List;

public interface ClubService {


    ClubResponse createClub(
            CreateClubRequest request,
            String authenticatedEmail
    );

    List<ClubResponse> getActiveClubs();

    ClubResponse getActiveClubById(Long clubId);

    List<ClubResponse> getMyClubs(String authenticatedEmail);
}
