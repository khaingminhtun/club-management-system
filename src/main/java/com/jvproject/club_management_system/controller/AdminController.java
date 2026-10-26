package com.jvproject.club_management_system.controller;

import com.jvproject.club_management_system.dto.admin.AdminProfileResponse;
import com.jvproject.club_management_system.dto.club.ClubRejectionRequest;
import com.jvproject.club_management_system.dto.club.PendingClubResponse;
import com.jvproject.club_management_system.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/me")
    public AdminProfileResponse getMyAdminProfile(
            Authentication authentication
    ) {
        return adminService.getMyAdminProfile(authentication.getName());
    }


    @GetMapping("/clubs/pending")
    public ResponseEntity<List<PendingClubResponse>> getPendingClubs() {

        List<PendingClubResponse> pendingClubs =
                adminService.getPendingClubs();

        return ResponseEntity.ok(pendingClubs);
    }

    @PatchMapping("/clubs/{clubId}/approve")
    public ResponseEntity<Void> approveClub(
            @PathVariable Long clubId
    ) {
        adminService.approveClub(clubId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/clubs/{clubId}/reject")
    public ResponseEntity<Void> rejectClub(
            @PathVariable Long clubId,
            @Valid @RequestBody ClubRejectionRequest request
    ) {
        adminService.rejectClub(clubId, request);
        return ResponseEntity.noContent().build();
    }
}