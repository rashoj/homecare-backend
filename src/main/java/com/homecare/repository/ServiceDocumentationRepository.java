package com.homecare.repository;

import com.homecare.entity.ServiceDocumentation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceDocumentationRepository extends JpaRepository<ServiceDocumentation, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    Optional<ServiceDocumentation> findByAppointmentId(Long appointmentId);

    Optional<ServiceDocumentation> findByAppointmentIdAndOrganizationId(
            Long appointmentId,
            Long organizationId
    );

    List<ServiceDocumentation> findByClientIdOrderBySubmittedAtDesc(Long clientId);

    List<ServiceDocumentation> findByOrganizationIdAndClientIdOrderBySubmittedAtDesc(
            Long organizationId,
            Long clientId
    );

    List<ServiceDocumentation> findByStatusOrderBySubmittedAtDesc(String status);

    List<ServiceDocumentation> findByOrganizationIdAndStatusOrderBySubmittedAtDesc(
            Long organizationId,
            String status
    );

    long countByStatus(String status);

    long countByOrganizationIdAndStatus(Long organizationId, String status);
}