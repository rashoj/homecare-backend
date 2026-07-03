package com.homecare.repository;

import com.homecare.entity.OpenShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OpenShiftRepository extends JpaRepository<OpenShift, Long> {

    List<OpenShift> findByOrganizationIdOrderByStartTimeAsc(Long organizationId);

    long countByOrganizationIdAndStatus(Long organizationId, String status);

    List<OpenShift> findByOrganizationIdAndStatusOrderByStartTimeAsc(
            Long organizationId,
            String status
    );

    Optional<OpenShift> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<OpenShift> findByOrganizationIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            Long organizationId,
            String status,
            LocalDateTime after
    );
    List<OpenShift> findTop5ByOrganizationIdAndStatusOrderByStartTimeAsc(
            Long organizationId,
            String status
    );

}