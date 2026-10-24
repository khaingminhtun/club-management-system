package com.jvproject.club_management_system.repository;

import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.MembershipStatus;
import com.jvproject.club_management_system.model.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRepository
        extends JpaRepository<ClubMembership, Long> {

    boolean existsByClub_IdAndUser_Id(Long clubId, Long userId);

    Optional<ClubMembership> findByClub_IdAndUser_Id(
            Long clubId,
            Long userId
    );

    List<ClubMembership> findAllByUser_IdAndStatus(
            Long userId,
            MembershipStatus status
    );

    List<ClubMembership> findAllByClub_IdAndStatus(
            Long clubId,
            MembershipStatus status
    );

    List<ClubMembership> findAllByClub_IdAndStatusAndRole(
            Long clubId,
            MembershipStatus status,
            ClubRole role
    );

    List<ClubMembership> findAllByClub_IdAndStatusOrderByRequestedAtAsc(
            Long clubId,
            MembershipStatus status
    );

    long countByClub_IdAndStatus(
            Long clubId,
            MembershipStatus status
    );
}