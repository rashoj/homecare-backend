package com.homecare.repository;

import com.homecare.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Incident> findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<Incident> findByStatusOrderByCreatedAtDesc(String status);

    List<Incident> findBySeverityOrderByCreatedAtDesc(String severity);

    List<Incident> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    long countByOrganizationIdAndStateReportableTrue(Long organizationId);

    long countByOrganizationIdAndSeverity(Long organizationId, String severity);

    long countByOrganizationIdAndStatus(Long organizationId, String status);

    long countByStatus(String status);

    long countBySeverity(String severity);

    long countByStateReportableTrue();

    long countByClientId(Long clientId);

    long countByClientIdAndSeverity(Long clientId, String severity);

    long countByClientIdAndStateReportableTrue(Long clientId);
    // IncidentRepository

    List<Incident> findTop5ByOrganizationIdAndStatusOrderByCreatedAtDesc(
            Long organizationId,
            String status
    );
    long countByOrganizationIdAndSeverityAndStatusIn(
            Long organizationId,
            String severity,
            List<String> statuses
    );
    long countByOrganizationIdAndStateReportableTrueAndStatusIn(
            Long organizationId,
            List<String> statuses
    );

    List<Incident> findTop5ByOrganizationIdAndSeverityInOrderByCreatedAtDesc(
            Long organizationId,
            List<String> severities
    );
    List<Incident> findTop5ByOrganizationIdAndSeverityInAndStatusInOrderByCreatedAtDesc(
            Long organizationId,
            List<String> severities,
            List<String> statuses
    );

    List<Incident> findTop5ByOrganizationIdAndStateReportableTrueAndStatusInOrderByCreatedAtDesc(
            Long organizationId,
            List<String> statuses
    );

    List<Incident> findTop5ByOrganizationIdAndStatusInOrderByCreatedAtDesc(
            Long organizationId,
            List<String> statuses
    );

    long countByOrganizationIdAndClientIdAndStatusIn(
            Long organizationId,
            Long clientId,
            List<String> statuses
    );

    long countByOrganizationIdAndClientIdAndSeverityInAndStatusIn(
            Long organizationId,
            Long clientId,
            List<String> severities,
            List<String> statuses
    );

    List<Incident> findTop5ByOrganizationIdAndClientIdAndStatusInOrderByCreatedAtDesc(
            Long organizationId,
            Long clientId,
            List<String> statuses
    );
}