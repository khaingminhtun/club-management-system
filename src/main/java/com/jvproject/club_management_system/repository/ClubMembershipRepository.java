package com.jvproject.club_management_system.repository;

import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.MembershipStatus;
import com.jvproject.club_management_system.model.Club;
import com.jvproject.club_management_system.model.ClubMembership;
import com.jvproject.club_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRepository
        extends JpaRepository<ClubMembership, Long> {



    List<ClubMembership> findAllByUser_IdAndStatus(
            Long userId,
            MembershipStatus status
    );

    List<ClubMembership> findAllByClub_IdAndStatusOrderByJoinedAtAsc(
            Long clubId,
            MembershipStatus status
    );

    List<ClubMembership> findAllByClub_IdAndStatusOrderByRequestedAtAsc(
            Long clubId,
            MembershipStatus status
    );

    Optional<ClubMembership> findByClub_IdAndUser_Id(Long clubId, Long userId);

    Optional<ClubMembership> findByIdAndClub_Id(Long id, Long clubId);

    Optional<ClubMembership> findByClubAndUserAndRole(
            Club club,
            User user,
            ClubRole role
    );

    long countByClub_IdAndStatus(
            Long clubId,
            MembershipStatus status
    );
}
