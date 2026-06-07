package com.homecare.repository;

import com.homecare.entity.EVVAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EVVAlertRepository extends JpaRepository<EVVAlert, Long> {

    List<EVVAlert> findByStatusOrderByCreatedAtDesc(String status);

    List<EVVAlert> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    List<EVVAlert> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
            Long organizationId,
            String status
    );

    List<EVVAlert> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    long countByOrganizationIdAndStatus(Long organizationId, String status);
}