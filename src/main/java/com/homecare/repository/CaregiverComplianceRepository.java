package com.homecare.repository;

import com.homecare.entity.CaregiverComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CaregiverComplianceRepository
        extends JpaRepository<CaregiverComplianceRecord, Long> {

    List<CaregiverComplianceRecord>
    findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<CaregiverComplianceRecord>
    findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<CaregiverComplianceRecord>
    findByOrganizationIdAndExpirationDateBeforeOrderByExpirationDateAsc(
            Long organizationId,
            LocalDate date
    );
}