package com.homecare.repository;

import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.ai.projection.CaregiverRiskProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<User> findByOrganizationIdAndRoleOrderByCreatedAtDesc(Long organizationId, Role role);


    boolean existsByEmail(String email);

    long countByRole(Role role);

    List<User> findByRole(Role role);

    long countByRoleName(String roleName);

    List<User> findTop10ByOrganizationIdAndRoleAndFullNameContainingIgnoreCaseOrderByFullNameAsc(
            Long organizationId,
            Role role,
            String fullName
    );

    List<User> findTop10ByOrganizationIdAndRoleAndActiveTrueOrderByFullNameAsc(
            Long organizationId,
            Role role
    );

    List<User> findByOrganizationIdAndRoleAndActiveTrue(
            Long organizationId,
            Role role
    );

    long countByOrganizationIdAndRoleAndActiveTrue(
            Long organizationId,
            Role role
    );
    @Query("""
        SELECT
            u.id AS caregiverId,

            (
                SELECT COUNT(e1)
                FROM EVVException e1
                WHERE e1.organization.id = :organizationId
                  AND e1.caregiver.id = u.id
                  AND e1.status = 'OPEN'
            ) AS openEvvIssues,

            (
                SELECT COUNT(e2)
                FROM EVVException e2
                WHERE e2.organization.id = :organizationId
                  AND e2.caregiver.id = u.id
                  AND e2.status = 'OPEN'
                  AND e2.severity IN ('HIGH', 'CRITICAL')
            ) AS highSeverityEvvIssues

        FROM User u

        WHERE u.organization.id = :organizationId
          AND u.role = com.homecare.entity.Role.CAREGIVER
          AND u.active = true
        """)
    List<CaregiverRiskProjection> findCaregiverRiskSummary(
            @Param("organizationId") Long organizationId
    );
}