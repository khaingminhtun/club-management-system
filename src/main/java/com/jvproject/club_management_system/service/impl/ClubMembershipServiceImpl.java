package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.club.membership.ClubMembershipResponse;
import com.jvproject.club_management_system.dto.club.membership.UpdateMembershipRoleRequest;
import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.ClubStatus;
import com.jvproject.club_management_system.enums.MembershipStatus;
import com.jvproject.club_management_system.exception.ConflictException;
import com.jvproject.club_management_system.exception.ResourceNotFoundException;
import com.jvproject.club_management_system.model.Club;
import com.jvproject.club_management_system.model.ClubMembership;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.ClubMembershipRepository;
import com.jvproject.club_management_system.repository.ClubRepository;
import com.jvproject.club_management_system.service.ClubMembershipService;
import com.jvproject.club_management_system.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMembershipServiceImpl implements ClubMembershipService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserValidationService userValidationService;

    @Override
    @Transactional
    public ClubMembershipResponse requestToJoin(Long clubId, String authenticatedEmail) {
        Club club = getActiveClub(clubId);
        User user = userValidationService.getVerifiedStudentByEmail(authenticatedEmail);

        ClubMembership membership = membershipRepository
                .findByClub_IdAndUser_Id(clubId, user.getId())
                .orElse(null);

        if (membership == null) {
            membership = ClubMembership.builder()
                    .club(club)
                    .user(user)
                    .role(ClubRole.MEMBER)
                    .status(MembershipStatus.PENDING)
                    .build();
        } else if (membership.getStatus() == MembershipStatus.PENDING
                || membership.getStatus() == MembershipStatus.APPROVED) {
            throw new ConflictException("A pending or approved membership already exists.");
        } else {
            membership.setRole(ClubRole.MEMBER);
            membership.setStatus(MembershipStatus.PENDING);
            membership.setRequestedAt(Instant.now());
            membership.setJoinedAt(null);
        }

        return toResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembershipResponse> getPendingRequests(Long clubId, String authenticatedEmail) {
        Club club = getActiveClub(clubId);
        requireLeaderOrCoLeader(club, authenticatedEmail);
        return membershipRepository.findAllByClub_IdAndStatusOrderByRequestedAtAsc(
                        clubId, MembershipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClubMembershipResponse approveRequest(Long clubId, Long membershipId, String authenticatedEmail) {
        Club club = getActiveClub(clubId);
        requireLeaderOrCoLeader(club, authenticatedEmail);
        ClubMembership membership = getPendingMembership(clubId, membershipId);
        membership.setStatus(MembershipStatus.APPROVED);
        membership.setJoinedAt(Instant.now());
        return toResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public ClubMembershipResponse rejectRequest(Long clubId, Long membershipId, String authenticatedEmail) {
        Club club = getActiveClub(clubId);
        requireLeaderOrCoLeader(club, authenticatedEmail);
        ClubMembership membership = getPendingMembership(clubId, membershipId);
        membership.setStatus(MembershipStatus.REJECTED);
        return toResponse(membershipRepository.save(membership));
    }

    @Override
    @Transactional
    public void leaveClub(Long clubId, String authenticatedEmail) {
        getActiveClub(clubId);
        User user = userValidationService.getUserByEmail(authenticatedEmail);
        ClubMembership membership = membershipRepository
                .findByClub_IdAndUser_Id(clubId, user.getId())
                .filter(item -> item.getStatus() == MembershipStatus.APPROVED)
                .orElseThrow(() -> new ConflictException("You do not have an approved membership in this club."));

        if (membership.getRole() == ClubRole.LEADER) {
            throw new ConflictException("The club leader cannot leave before leadership is transferred.");
        }

        membership.setStatus(MembershipStatus.LEFT);
        membership.setJoinedAt(null);
        membershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubMembershipResponse getMyMembership(Long clubId, String authenticatedEmail) {
        clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        User user = userValidationService.getUserByEmail(authenticatedEmail);
        ClubMembership membership = membershipRepository
                .findByClub_IdAndUser_Id(clubId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for this user."));
        return toResponse(membership);
    }

    @Override
    @Transactional
    public ClubMembershipResponse updateMemberRole(
            Long clubId,
            Long membershipId,
            UpdateMembershipRoleRequest request,
            String authenticatedEmail
    ) {
        Club club = getActiveClub(clubId);
        requireLeader(club, authenticatedEmail);
        ClubMembership membership = membershipRepository.findByIdAndClub_Id(membershipId, clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + membershipId));
        if (membership.getStatus() != MembershipStatus.APPROVED) {
            throw new ConflictException("Only approved members can have their role changed.");
        }
        if (membership.getRole() == ClubRole.LEADER || request.role() == ClubRole.LEADER) {
            throw new ConflictException("Leadership cannot be assigned or changed through this endpoint.");
        }

        membership.setRole(request.role());
        return toResponse(membershipRepository.save(membership));
    }

    private Club getActiveClub(Long clubId) {
        return clubRepository.findById(clubId)
                .filter(club -> club.getStatus() == ClubStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active club not found: " + clubId));
    }

    private ClubMembership getPendingMembership(Long clubId, Long membershipId) {
        ClubMembership membership = membershipRepository.findByIdAndClub_Id(membershipId, clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership request not found: " + membershipId));
        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ConflictException("Only pending membership requests can be reviewed.");
        }
        return membership;
    }

    private void requireLeaderOrCoLeader(Club club, String email) {
        ClubMembership actorMembership = getActorMembership(club, email);
        if (actorMembership.getRole() != ClubRole.LEADER
                && actorMembership.getRole() != ClubRole.CO_LEADER) {
            throw new AccessDeniedException("Club leader privileges are required.");
        }
    }

    private void requireLeader(Club club, String email) {
        if (getActorMembership(club, email).getRole() != ClubRole.LEADER) {
            throw new AccessDeniedException("Only the club leader can change member roles.");
        }
    }

    private ClubMembership getActorMembership(Club club, String email) {
        User actor = userValidationService.getUserByEmail(email);
        return membershipRepository.findByClub_IdAndUser_Id(club.getId(), actor.getId())
                .filter(item -> item.getStatus() == MembershipStatus.APPROVED)
                .orElseThrow(() -> new AccessDeniedException("Approved club leadership membership is required."));
    }

    private ClubMembershipResponse toResponse(ClubMembership membership) {
        User user = membership.getUser();
        return new ClubMembershipResponse(
                membership.getId(),
                membership.getClub().getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                membership.getRole(),
                membership.getStatus(),
                membership.getRequestedAt(),
                membership.getJoinedAt()
        );
    }
}
