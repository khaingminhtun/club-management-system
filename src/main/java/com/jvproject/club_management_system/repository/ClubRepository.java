package com.jvproject.club_management_system.repository;

import com.jvproject.club_management_system.enums.ClubStatus;
import com.jvproject.club_management_system.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club,Long> {

    boolean existsBySlug(String slug);

    List<Club> findByStatusOrderByCreatedAtAsc(ClubStatus status);

    Optional<Club> findByIdAndStatus(Long id, ClubStatus status);


    List<Club> findAllByStatus(ClubStatus status);



}
