package com.jvproject.club_management_system.repository;

import com.jvproject.club_management_system.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

     @EntityGraph(attributePaths = "roles")
     Optional<User> findByEmailIgnoreCase(String email);

     boolean existsByEmailIgnoreCase(String email);

     boolean existsByStudentId(String studentId);

}
