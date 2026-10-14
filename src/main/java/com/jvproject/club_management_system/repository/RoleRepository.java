package com.jvproject.club_management_system.repository;

import com.jvproject.club_management_system.enums.RoleName;
import com.jvproject.club_management_system.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(RoleName name);
}
