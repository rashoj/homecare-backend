package com.homecare.repository;

import com.homecare.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    List<FraudAlert> findByOrganizationIdOrderByDetectedAtDesc(Long organizationId);

    List<FraudAlert> findByOrganizationIdAndStatusOrderByDetectedAtDesc(
            Long organizationId,
            String status
    );

    long countByOrganizationIdAndStatus(Long organizationId, String status);



    long countByOrganizationIdAndSeverityAndStatus(
            Long organizationId,
            String severity,
            String status
    );
    boolean existsByOrganizationIdAndAlertTypeAndVisitIdAndStatus(
            Long organizationId,
            String alertType,
            Long visitId,
            String status
    );

}