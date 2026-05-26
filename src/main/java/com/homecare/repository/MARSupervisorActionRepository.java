package com.homecare.repository;

import com.homecare.entity.MARSupervisorAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MARSupervisorActionRepository
        extends JpaRepository<MARSupervisorAction, Long> {

    List<MARSupervisorAction> findByMedicationLogIdOrderByCreatedAtDesc(
            Long medicationLogId
    );
}