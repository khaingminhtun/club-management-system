package com.jvproject.club_management_system.repository;

import com.jvproject.club_management_system.model.StudentRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRegistryRepository extends JpaRepository<StudentRegistry,Long> {

    Optional<StudentRegistry>
    findByStudentIdAndUniversityEmailIgnoreCase(
            String studentId,
            String universityEmail
    );
}
