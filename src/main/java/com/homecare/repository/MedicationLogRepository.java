package com.homecare.repository;

import com.homecare.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {

    List<MedicationLog> findByClientId(Long clientId);

    List<MedicationLog> findByMedicationId(Long medicationId);

    long countByCaregiverId(Long caregiverId);

    long countByStatus(String status);

    long countByClientIdAndStatus(Long clientId, String status);

    List<MedicationLog> findByStatusOrderByCreatedAtDesc(String status);

    List<MedicationLog> findByOrganizationIdOrderByScheduledAtDesc(Long organizationId);

    List<MedicationLog> findByOrganizationIdAndClientId(Long organizationId, Long clientId);

    List<MedicationLog> findByOrganizationIdAndMedicationId(Long organizationId, Long medicationId);

    long countByOrganizationIdAndStatus(Long organizationId, String status);


    boolean existsByMedicationIdAndClientIdAndScheduledAt(
            Long medicationId,
            Long clientId,
            java.time.LocalDateTime scheduledAt
    );
}