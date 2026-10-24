
package com.jvproject.club_management_system.model;

import com.jvproject.club_management_system.enums.ClubRole;
import com.jvproject.club_management_system.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "club_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_club_memberships_club_user",
                        columnNames = {"club_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_memberships_user_status",
                        columnList = "user_id, status"
                ),
                @Index(
                        name = "idx_memberships_club_status",
                        columnList = "club_id, status"
                )
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClubRole role = ClubRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }
}
