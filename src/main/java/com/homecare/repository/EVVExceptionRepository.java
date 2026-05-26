package com.homecare.repository;

import com.homecare.entity.EVVException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EVVExceptionRepository extends JpaRepository<EVVException, Long> {

    List<EVVException> findByStatusOrderByCreatedAtDesc(String status);

    List<EVVException> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    List<EVVException> findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<EVVException> findByClientIdOrderByCreatedAtDesc(Long clientId);

    long countByStatus(String status);

    long countBySeverity(String severity);


    boolean existsByAppointmentIdAndClockRecordIdAndExceptionType(
            Long appointmentId,
            Long clockRecordId,
            String exceptionType
    );
}