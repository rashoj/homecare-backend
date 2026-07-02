package com.homecare.repository;

import com.homecare.entity.CaregiverTimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaregiverTimeEntryRepository extends JpaRepository<CaregiverTimeEntry, Long> {

    Optional<CaregiverTimeEntry> findByCaregiverIdAndStatus(
            Long caregiverId,
            String status
    );

    List<CaregiverTimeEntry> findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<CaregiverTimeEntry> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    boolean existsByCaregiverIdAndStatus(Long caregiverId, String status);

}