package com.homecare.repository;

import com.homecare.entity.VisitNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitNoteRepository extends JpaRepository<VisitNote, Long> {

    Optional<VisitNote> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    List<VisitNote> findByClientId(Long clientId);

    List<VisitNote> findByCaregiverId(Long caregiverId);

    long countByCaregiverId(Long caregiverId);

    long countByIncidentReportedTrue();

    long countByClientIdAndIncidentReportedTrue(Long clientId);
}