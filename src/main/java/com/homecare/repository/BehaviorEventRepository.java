package com.homecare.repository;

import com.homecare.entity.BehaviorEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BehaviorEventRepository extends JpaRepository<BehaviorEvent, Long> {

    List<BehaviorEvent> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<BehaviorEvent> findByServiceDocumentationIdOrderByCreatedAtDesc(Long serviceDocumentationId);

    List<BehaviorEvent> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
}