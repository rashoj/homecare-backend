package com.homecare.service;

import com.homecare.dto.CaregiverAssignmentResponse;
import com.homecare.dto.CaregiverDto;
import com.homecare.dto.ClientDto;
import com.homecare.entity.Appointment;
import com.homecare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CaregiverAssignmentService {

    private final AppointmentRepository appointmentRepository;

    public CaregiverAssignmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public CaregiverAssignmentResponse getTodayAssignment(Long caregiverId) {

        List<String> activeStatuses = List.of("SCHEDULED", "IN_PROGRESS");

        Appointment appointment = appointmentRepository
                .findFirstByCaregiverIdAndStartTimeBetweenAndStatusInOrderByStartTimeDesc(
                        caregiverId,
                        LocalDate.now().atStartOfDay(),
                        LocalDate.now().atTime(LocalTime.MAX),
                        activeStatuses
                )
                .orElseGet(() ->
                        appointmentRepository
                                .findFirstByCaregiverIdAndStartTimeBetweenOrderByStartTimeDesc(
                                        caregiverId,
                                        LocalDate.now().atStartOfDay(),
                                        LocalDate.now().atTime(LocalTime.MAX)
                                )
                                .orElseThrow(() -> new RuntimeException("No appointment found for today."))
                );


        return new CaregiverAssignmentResponse(
                appointment.getId(),
                appointment.getStatus(),
                new CaregiverDto(
                        appointment.getCaregiver().getId(),
                        appointment.getCaregiver().getFullName(),
                        appointment.getCaregiver().getRole().name()
                ),
                new ClientDto(
                        appointment.getClient().getId(),
                        appointment.getClient().getFullName(),
                        appointment.getClient().getAddress(),
                        appointment.getClient().getLatitude(),
                        appointment.getClient().getLongitude()
                )
        );
    }
}