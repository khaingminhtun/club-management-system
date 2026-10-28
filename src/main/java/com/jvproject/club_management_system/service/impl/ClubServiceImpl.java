package com.jvproject.club_management_system.service.impl;

import com.jvproject.club_management_system.dto.club.ClubResponse;
import com.jvproject.club_management_system.dto.club.ClubMemberResponse;
import com.jvproject.club_management_system.dto.club.CreateClubRequest;
import com.jvproject.club_management_system.dto.club.UpdateClubRequest;
import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.ClubStatus;
import com.jvproject.club_management_system.enums.MembershipStatus;
import com.jvproject.club_management_system.exception.ResourceNotFoundException;
import com.jvproject.club_management_system.model.Club;
import com.jvproject.club_management_system.model.ClubMembership;
import com.jvproject.club_management_system.model.User;
import com.jvproject.club_management_system.repository.ClubMembershipRepository;
import com.jvproject.club_management_system.repository.ClubRepository;
import com.jvproject.club_management_system.service.ClubService;
import com.jvproject.club_management_system.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserValidationService userValidationService;

    @Override
    @Transactional
    public ClubResponse createClub(
            CreateClubRequest request,
            String authenticatedEmail) {

        User user = userValidationService
                .getVerifiedStudentByEmail(authenticatedEmail);

        Club club = Club.builder()
                .createdBy(user)
                .name(request.name().trim())
                .slug(generateUniqueSlug(request.name()))
                .description(request.description().trim())
                .logoUrl(normalizeOptional(request.logoUrl()))
                .coverImageUrl(normalizeOptional(request.coverImageUrl()))
                .status(ClubStatus.PENDING)
                .build();

        Club savedClub = clubRepository.save(club);

        ClubMembership founderMembership = ClubMembership.builder()
                .club(savedClub)
                .user(user)
                .role(ClubRole.LEADER)
                .status(MembershipStatus.PENDING)
                .build();

        membershipRepository.save(founderMembership);

        return toResponse(savedClub, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubResponse> getActiveClubs() {
        return clubRepository.findAllByStatus(ClubStatus.ACTIVE)
                .stream()
                .map(club -> toResponse(
                        club,
                        membershipRepository.countByClub_IdAndStatus(
                                club.getId(),
                                MembershipStatus.APPROVED
                        )
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClubResponse getActiveClubById(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .filter(c -> c.getStatus() == ClubStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active club not found: " + clubId
                        )
                );

        long memberCount = membershipRepository.countByClub_IdAndStatus(
                clubId,
                MembershipStatus.APPROVED
        );

        return toResponse(club, memberCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubResponse> getMyClubs(String authenticatedEmail) {
        User user = userValidationService
                .getUserByEmail(authenticatedEmail);

        return membershipRepository.findAllByUser_IdAndStatus(
                        user.getId(),
                        MembershipStatus.APPROVED
                )
                .stream()
                .map(ClubMembership::getClub)
                .map(club -> toResponse(
                        club,
                        membershipRepository.countByClub_IdAndStatus(
                                club.getId(),
                                MembershipStatus.APPROVED
                        )
                ))
                .toList();
    }

    @Override
    @Transactional
    public ClubResponse updateClub(
            Long clubId,
            UpdateClubRequest request,
            String authenticatedEmail
    ) {
        User user = userValidationService.getUserByEmail(authenticatedEmail);
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));

        if (!club.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the club founder can update this club.");
        }

        if (request.name() != null) {
            String name = request.name().trim();
            club.setName(name);
            club.setSlug(generateUniqueSlug(name, clubId));
        }
        if (request.description() != null) {
            club.setDescription(request.description().trim());
        }
        if (request.logoUrl() != null) {
            club.setLogoUrl(normalizeOptional(request.logoUrl()));
        }
        if (request.coverImageUrl() != null) {
            club.setCoverImageUrl(normalizeOptional(request.coverImageUrl()));
        }

        long memberCount = membershipRepository.countByClub_IdAndStatus(
                clubId,
                MembershipStatus.APPROVED
        );
        return toResponse(clubRepository.save(club), memberCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMemberResponse> getClubMembers(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .filter(item -> item.getStatus() == ClubStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active club not found: " + clubId
                ));

        return membershipRepository.findAllByClub_IdAndStatusOrderByJoinedAtAsc(
                        club.getId(), MembershipStatus.APPROVED)
                .stream()
                .map(membership -> new ClubMemberResponse(
                        membership.getUser().getId(),
                        membership.getUser().getFullName(),
                        membership.getUser().getEmail(),
                        membership.getRole(),
                        membership.getJoinedAt()
                ))
                .toList();
    }

    private String generateUniqueSlug(String name) {
        return generateUniqueSlug(name, null);
    }

    private String generateUniqueSlug(String name, Long excludedClubId) {
        String baseSlug = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (baseSlug.isBlank()) {
            baseSlug = "club";
        }

        String slug = baseSlug;
        int suffix = 2;

        while (excludedClubId == null
                ? clubRepository.existsBySlug(slug)
                : clubRepository.existsBySlugAndIdNot(slug, excludedClubId)) {
            slug = baseSlug + "-" + suffix++;
        }

        return slug;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    private ClubResponse toResponse(Club club, long memberCount) {
        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getSlug(),
                club.getDescription(),
                club.getLogoUrl(),
                club.getCoverImageUrl(),
                club.getStatus(),
                club.getCreatedBy().getId(),
                club.getCreatedBy().getFullName(),
                memberCount,
                club.getCreatedAt(),
                club.getUpdatedAt()
        );
    }
}
