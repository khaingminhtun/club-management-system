package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.club.membership.ClubMembershipResponse;
import com.jvproject.club_management_system.dto.club.membership.UpdateMembershipRoleRequest;

import java.util.List;

public interface ClubMembershipService {

    ClubMembershipResponse requestToJoin(Long clubId, String authenticatedEmail);

    List<ClubMembershipResponse> getPendingRequests(Long clubId, String authenticatedEmail);

    ClubMembershipResponse approveRequest(Long clubId, Long membershipId, String authenticatedEmail);

    ClubMembershipResponse rejectRequest(Long clubId, Long membershipId, String authenticatedEmail);

    void leaveClub(Long clubId, String authenticatedEmail);

    ClubMembershipResponse getMyMembership(Long clubId, String authenticatedEmail);

    ClubMembershipResponse updateMemberRole(
            Long clubId,
            Long membershipId,
            UpdateMembershipRoleRequest request,
            String authenticatedEmail
    );
}
