package com.homecare.repository;

import com.homecare.entity.AppointmentRescheduleRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRescheduleRepository
        extends JpaRepository<AppointmentRescheduleRequest, Long> {

    List<AppointmentRescheduleRequest> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<AppointmentRescheduleRequest> findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<AppointmentRescheduleRequest> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    List<AppointmentRescheduleRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<AppointmentRescheduleRequest> findAllByOrderByCreatedAtDesc();
}