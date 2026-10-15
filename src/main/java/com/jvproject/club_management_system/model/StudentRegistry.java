package com.jvproject.club_management_system.model;



import com.jvproject.club_management_system.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "student_registry")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentRegistry {

    @Id
    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "university_email",
            nullable = false, unique = true, length = 254)
    private String universityEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false)
    @Builder.Default
    private EnrollmentStatus enrollmentStatus = EnrollmentStatus.ACTIVE;

    @Column(name = "source_batch_id", nullable = false, length = 100)
    private String sourceBatchId;

    @Column(name = "imported_at", nullable = false, updatable = false)
    private Instant importedAt;

    @PrePersist
    protected void onCreate() {
        importedAt = Instant.now();
    }
}
