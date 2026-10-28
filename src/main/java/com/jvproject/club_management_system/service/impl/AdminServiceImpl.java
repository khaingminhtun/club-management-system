package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.admin.AdminProfileResponse;
import com.jvproject.club_management_system.dto.club.ClubRejectionRequest;
import com.jvproject.club_management_system.dto.club.PendingClubResponse;
import com.jvproject.club_management_system.dto.club.ClubResponse;
import com.jvproject.club_management_system.dto.user.UserResponse;
import com.jvproject.club_management_system.dto.user.UpdateUserStatusRequest;
import com.jvproject.club_management_system.enums.AccountStatus;
import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.ClubStatus;
import com.jvproject.club_management_system.enums.MembershipStatus;
import com.jvproject.club_management_system.enums.RoleName;
import com.jvproject.club_management_system.exception.ConflictException;
import com.jvproject.club_management_system.exception.ResourceNotFoundException;
import com.jvproject.club_management_system.model.Club;
import com.jvproject.club_management_system.model.ClubMembership;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.ClubMembershipRepository;
import com.jvproject.club_management_system.repository.ClubRepository;
import com.jvproject.club_management_system.repository.UserRepository;
import com.jvproject.club_management_system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getMyAdminProfile(String email) {
        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user account was not found."
                        )
                );

        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);

        if (!isAdmin) {
            throw new AccessDeniedException(
                    "Administrator privileges are required."
            );
        }

        return new AdminProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                RoleName.ADMIN.name()
        );
    }



    @Override
    @Transactional(readOnly = true)
    public List<PendingClubResponse> getPendingClubs() {

        List<Club> pendingClubs =
                clubRepository.findByStatusOrderByCreatedAtAsc(
                        ClubStatus.PENDING
                );

        return pendingClubs.stream()
                .map(this::mapToPendingClubResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubResponse> getClubs() {
        return clubRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToClubResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClubResponse getClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        return mapToClubResponse(club);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(request.status());
        user.setEnabled(request.status() == AccountStatus.ACTIVE);
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void approveClub(Long clubId) {
        Club club = clubRepository
                .findByIdAndStatus(clubId, ClubStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pending club request not found"
                ));

        ClubMembership founderMembership = clubMembershipRepository
                .findByClubAndUserAndRole(
                        club,
                        club.getCreatedBy(),
                        ClubRole.LEADER
                )
                .orElseThrow(() -> new ConflictException(
                        "Founder's leader membership was not found"
                ));

        club.setStatus(ClubStatus.ACTIVE);
        club.setRejectionReason(null);
        founderMembership.setStatus(MembershipStatus.APPROVED);

        clubRepository.save(club);
        clubMembershipRepository.save(founderMembership);
    }

    @Transactional
    public void rejectClub(Long clubId, ClubRejectionRequest request) {
        Club club = clubRepository
                .findByIdAndStatus(clubId, ClubStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pending club request not found"
                ));

        club.setStatus(ClubStatus.REJECTED);
        club.setRejectionReason(request.reason().trim());

        clubRepository.save(club);
    }

    private PendingClubResponse mapToPendingClubResponse(Club club) {

        return new PendingClubResponse(
                club.getId(),
                club.getName(),
                club.getSlug(),
                club.getDescription(),
                club.getLogoUrl(),
                club.getCoverImageUrl(),
                club.getStatus().name(),
                club.getCreatedAt(),
                club.getCreatedBy().getId(),
                club.getCreatedBy().getFullName(),
                club.getCreatedBy().getEmail()
        );
    }

    private ClubResponse mapToClubResponse(Club club) {
        return new ClubResponse(
                club.getId(), club.getName(), club.getSlug(), club.getDescription(),
                club.getLogoUrl(), club.getCoverImageUrl(), club.getStatus(),
                club.getCreatedBy().getId(), club.getCreatedBy().getFullName(),
                clubMembershipRepository.countByClub_IdAndStatus(
                        club.getId(), MembershipStatus.APPROVED),
                club.getCreatedAt(), club.getUpdatedAt()
        );
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(), user.getStudentId(), user.getFullName(), user.getEmail(),
                user.isEnabled(), user.getStatus(), user.getEmailVerifiedAt(),
                user.getStudentVerifiedAt(), user.getCreatedAt()
        );
    }
}
