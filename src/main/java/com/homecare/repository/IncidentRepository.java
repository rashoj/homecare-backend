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

    long countByOrganizationIdAndSeverity(Long organizationId, String severity);

    long countByOrganizationIdAndStatus(Long organizationId, String status);

    long countByStatus(String status);

    long countBySeverity(String severity);

    long countByStateReportableTrue();

    long countByClientId(Long clientId);

    long countByClientIdAndSeverity(Long clientId, String severity);

    long countByClientIdAndStateReportableTrue(Long clientId);
    // IncidentRepository
}