package com.homecare.repository;

import com.homecare.entity.Client;
import com.homecare.ai.projection.ClientRiskProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByActiveTrue();

    List<Client> findByOrganizationIdAndActiveTrue(
            Long organizationId
    );

    Optional<Client> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<Client> findTop10ByOrganizationIdAndFullNameContainingIgnoreCaseOrderByFullNameAsc(
            Long organizationId,
            String fullName
    );

    List<Client> findTop10ByOrganizationIdAndActiveTrueOrderByFullNameAsc(
            Long organizationId
    );

    long countByOrganizationIdAndActiveTrue(
            Long organizationId
    );

    @Query("""
        SELECT
            c.id AS clientId,

            (
                SELECT COUNT(e1)
                FROM EVVException e1
                WHERE e1.organization.id = :organizationId
                  AND e1.client.id = c.id
                  AND e1.status = 'OPEN'
            ) AS openEvvIssues,

            (
                SELECT COUNT(e2)
                FROM EVVException e2
                WHERE e2.organization.id = :organizationId
                  AND e2.client.id = c.id
                  AND e2.status = 'OPEN'
                  AND e2.severity IN ('HIGH', 'CRITICAL')
            ) AS highSeverityEvvIssues,

            (
                SELECT COUNT(i1)
                FROM Incident i1
                WHERE i1.organization.id = :organizationId
                  AND i1.client.id = c.id
                  AND i1.status IN ('SUBMITTED', 'UNDER_REVIEW')
            ) AS activeIncidents,

            (
                SELECT COUNT(i2)
                FROM Incident i2
                WHERE i2.organization.id = :organizationId
                  AND i2.client.id = c.id
                  AND i2.status IN ('SUBMITTED', 'UNDER_REVIEW')
                  AND i2.severity IN ('HIGH', 'CRITICAL')
            ) AS highRiskIncidents

        FROM Client c

        WHERE c.organization.id = :organizationId
          AND c.active = true
        """)
    List<ClientRiskProjection> findClientRiskSummary(
            @Param("organizationId") Long organizationId
    );
}