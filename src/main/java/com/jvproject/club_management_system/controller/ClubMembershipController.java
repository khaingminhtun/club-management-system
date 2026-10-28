package com.jvproject.club_management_system.controller;

import com.jvproject.club_management_system.dto.club.membership.ClubMembershipResponse;
import com.jvproject.club_management_system.dto.club.membership.UpdateMembershipRoleRequest;
import com.jvproject.club_management_system.service.ClubMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/memberships")
@RequiredArgsConstructor
public class ClubMembershipController {

    private final ClubMembershipService membershipService;

    @PostMapping({"", "/"})
    public ResponseEntity<ClubMembershipResponse> requestToJoin(
            @PathVariable Long clubId,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                membershipService.requestToJoin(clubId, authentication.getName())
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ClubMembershipResponse>> getPendingRequests(
            @PathVariable Long clubId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                membershipService.getPendingRequests(clubId, authentication.getName())
        );
    }

    @PatchMapping("/{membershipId}/approve")
    public ResponseEntity<ClubMembershipResponse> approveRequest(
            @PathVariable Long clubId,
            @PathVariable Long membershipId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(membershipService.approveRequest(
                clubId, membershipId, authentication.getName()));
    }

    @PatchMapping("/{membershipId}/reject")
    public ResponseEntity<ClubMembershipResponse> rejectRequest(
            @PathVariable Long clubId,
            @PathVariable Long membershipId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(membershipService.rejectRequest(
                clubId, membershipId, authentication.getName()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveClub(
            @PathVariable Long clubId,
            Authentication authentication
    ) {
        membershipService.leaveClub(clubId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ClubMembershipResponse> getMyMembership(
            @PathVariable Long clubId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                membershipService.getMyMembership(clubId, authentication.getName())
        );
    }

    @PatchMapping("/{membershipId}/role")
    public ResponseEntity<ClubMembershipResponse> updateMemberRole(
            @PathVariable Long clubId,
            @PathVariable Long membershipId,
            @Valid @RequestBody UpdateMembershipRoleRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(membershipService.updateMemberRole(
                clubId, membershipId, request, authentication.getName()));
    }
}
