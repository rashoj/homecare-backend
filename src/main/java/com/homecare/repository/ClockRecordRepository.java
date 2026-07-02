package com.homecare.repository;

import com.homecare.entity.ClockRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClockRecordRepository extends JpaRepository<ClockRecord, Long> {

    Optional<ClockRecord> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    long countByAppointmentCaregiverId(Long caregiverId);

    List<ClockRecord> findByAppointmentCaregiverId(Long caregiverId);

    List<ClockRecord> findByAppointmentClientId(Long clientId);

    List<ClockRecord> findByClockInTimeBeforeAndClockOutTimeIsNull(
            LocalDateTime cutoff
    );

    long countByStatus(String status);

    long countByAppointmentClientIdAndStatus(Long clientId, String status);
    long countByAppointmentOrganizationIdAndStatus(Long organizationId, String status);

}