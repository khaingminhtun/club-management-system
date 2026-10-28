
package com.jvproject.club_management_system.controller;

import com.jvproject.club_management_system.dto.club.CreateClubRequest;
import com.jvproject.club_management_system.dto.club.ClubResponse;
import com.jvproject.club_management_system.dto.club.ClubMemberResponse;
import com.jvproject.club_management_system.dto.club.UpdateClubRequest;
import com.jvproject.club_management_system.service.ClubService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    /**
     * Submit a club creation request.
     * The authenticated student becomes the proposed club leader.
     */
    @PostMapping
    public ResponseEntity<ClubResponse> createClub(
            @Valid @RequestBody CreateClubRequest request,
            Authentication authentication
    ) {
        ClubResponse response = clubService.createClub(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get all active clubs.
     * Only clubs approved by an administrator should be returned.
     */
    @GetMapping
    public ResponseEntity<List<ClubResponse>> getActiveClubs() {
        return ResponseEntity.ok(
                clubService.getActiveClubs()
        );
    }

    /**
     * Get details of one active club.
     */
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponse> getActiveClubById(
            @PathVariable Long clubId
    ) {
        return ResponseEntity.ok(
                clubService.getActiveClubById(clubId)
        );
    }

    @PatchMapping("/{clubId}")
    public ResponseEntity<ClubResponse> updateClub(
            @PathVariable Long clubId,
            @Valid @RequestBody UpdateClubRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(clubService.updateClub(
                clubId, request, authentication.getName()));
    }

    @GetMapping("/{clubId}/members")
    public ResponseEntity<List<ClubMemberResponse>> getClubMembers(
            @PathVariable Long clubId
    ) {
        return ResponseEntity.ok(clubService.getClubMembers(clubId));
    }

    /**
     * Get the authenticated student's approved club memberships.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ClubResponse>> getMyClubs(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                clubService.getMyClubs(authentication.getName())
        );
    }
}
