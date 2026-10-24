package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.club.ClubResponse;
import com.jvproject.club_management_system.dto.club.CreateClubRequest;
import com.jvproject.club_management_system.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {
    @Override
    public ClubResponse createClub(CreateClubRequest request, String authenticatedEmail) {
        return null;
    }

    @Override
    public List<ClubResponse> getActiveClubs() {
        return List.of();
    }

    @Override
    public ClubResponse getActiveClubById(Long clubId) {
        return null;
    }

    @Override
    public List<ClubResponse> getMyClubs(String authenticatedEmail) {
        return List.of();
    }
}
