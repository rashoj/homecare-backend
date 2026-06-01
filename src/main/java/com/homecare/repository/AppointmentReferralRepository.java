package com.homecare.repository;

import com.homecare.entity.AppointmentReferral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentReferralRepository extends JpaRepository<AppointmentReferral, Long> {

    List<AppointmentReferral> findByStatusOrderByCreatedAtDesc(String status);

    List<AppointmentReferral> findByCaregiverIdOrderByCreatedAtDesc(Long caregiverId);

    List<AppointmentReferral> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<AppointmentReferral> findAllByOrderByCreatedAtDesc();
}