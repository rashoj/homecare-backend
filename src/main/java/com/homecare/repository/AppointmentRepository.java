package com.homecare.repository;

import com.homecare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    long countByCompletedTrue();
    long countByCompletedFalse();
    long countByCaregiverId(Long caregiverId);

    long countByCaregiverIdAndCompletedTrue(Long caregiverId);

    long countByCaregiverIdAndCompletedFalse(Long caregiverId);



    List<Appointment> findByClientId(Long clientId);

    List<Appointment> findByCaregiverId(Long caregiverId);

    List<Appointment> findByClientIdOrderByStartTimeDesc(Long clientId);

    List<Appointment> findByCompletedTrue();

    List<Appointment> findByClientIdAndCompletedTrue(Long clientId);

    List<Appointment> findByCaregiverIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long caregiverId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<Appointment> findByClientIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long clientId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );


    //    Optional<Appointment> findFirstByCaregiverIdAndStartTimeBetweenAndCompletedFalseOrderByStartTimeAsc(
//            Long caregiverId,
//            LocalDateTime start,
//            LocalDateTime end
//    );
    Optional<Appointment> findFirstByCaregiverIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long caregiverId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Appointment> findFirstByCaregiverIdAndStartTimeBetweenOrderByStartTimeDesc(
            Long caregiverId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Appointment> findFirstByCaregiverIdAndStartTimeBetweenAndStatusInOrderByStartTimeAsc(
            Long caregiverId,
            LocalDateTime start,
            LocalDateTime end,
            List<String> statuses
    );

    Optional<Appointment> findFirstByCaregiverIdAndStartTimeBetweenAndStatusInOrderByStartTimeDesc(
            Long caregiverId,
            LocalDateTime start,
            LocalDateTime end,
            List<String> statuses
    );

}