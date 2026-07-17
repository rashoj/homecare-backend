package com.homecare.repository;

import com.homecare.entity.EVVException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EVVExceptionRepository extends JpaRepository<EVVException, Long> {

    List<EVVException> findByStatusOrderByCreatedAtDesc(String status);

    List<EVVException> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    List<EVVException> findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<EVVException> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<EVVException> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<EVVException> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
            Long organizationId,
            String status
    );

    List<EVVException> findByOrganizationIdAndClientIdOrderByCreatedAtDesc(
            Long organizationId,
            Long clientId
    );

    List<EVVException> findTop5ByOrganizationIdAndStatusOrderByCreatedAtDesc(
            Long organizationId,
            String status
    );

    List<EVVException> findTop5ByOrganizationIdAndSeverityInAndStatusOrderByCreatedAtDesc(
            Long organizationId,
            List<String> severities,
            String status
    );
    long countByOrganizationIdAndSeverityAndStatus(
            Long organizationId,
            String severity,
            String status
    );

    Optional<EVVException> findByIdAndOrganizationId(Long id, Long organizationId);

    long countByStatus(String status);

    long countBySeverity(String severity);

    long countByOrganizationIdAndStatus(Long organizationId, String status);

    long countByOrganizationIdAndSeverity(Long organizationId, String severity);

    boolean existsByAppointmentIdAndClockRecordIdAndExceptionType(
            Long appointmentId,
            Long clockRecordId,
            String exceptionType
    );

    long countByOrganizationIdAndClientIdAndStatus(
            Long organizationId,
            Long clientId,
            String status
    );

    long countByOrganizationIdAndClientIdAndSeverityInAndStatus(
            Long organizationId,
            Long clientId,
            List<String> severities,
            String status
    );

    long countByOrganizationIdAndCaregiverIdAndStatus(
            Long organizationId,
            Long caregiverId,
            String status
    );

    long countByOrganizationIdAndCaregiverIdAndSeverityInAndStatus(
            Long organizationId,
            Long caregiverId,
            List<String> severities,
            String status
    );
}