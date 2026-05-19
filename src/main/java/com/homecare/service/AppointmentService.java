package com.homecare.service;

import com.homecare.dto.AppointmentRequest;
import com.homecare.dto.AppointmentResponse;
import com.homecare.entity.Appointment;
import com.homecare.entity.Client;
import com.homecare.entity.User;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.homecare.dto.AppointmentStatusUpdateRequest;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientCaregiverRepository clientCaregiverRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            ClientCaregiverRepository clientCaregiverRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.clientCaregiverRepository = clientCaregiverRepository;
    }

    public AppointmentResponse createAppointment(AppointmentRequest request) {

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));

        boolean assigned = clientCaregiverRepository
                .existsByClientIdAndCaregiverIdAndActiveTrue(
                        client.getId(),
                        caregiver.getId()
                );

        if (!assigned) {
            throw new RuntimeException(
                    "Caregiver must be assigned to the client before scheduling."
            );
        }

        validateAppointmentTimes(request);

        Appointment appointment = Appointment.builder()
                .client(client)
                .caregiver(caregiver)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .serviceType(defaultValue(request.getServiceType(), "PERSONAL_CARE"))
                .shiftType(defaultValue(request.getShiftType(), "REGULAR"))
                .status(defaultValue(request.getStatus(), "SCHEDULED"))
                .evvRequired(request.getEvvRequired() != null ? request.getEvvRequired() : true)
                .billable(request.getBillable() != null ? request.getBillable() : true)
                .notes(request.getNotes())
                .completed(false)
                .build();

        return mapToResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByClient(Long clientId) {
        return appointmentRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByCaregiver(Long caregiverId) {
        return appointmentRepository.findByCaregiverId(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateAppointmentTimes(AppointmentRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required.");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time.");
        }
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.toUpperCase();
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .clientId(appointment.getClient().getId())
                .clientName(appointment.getClient().getFullName())
                .caregiverId(appointment.getCaregiver().getId())
                .caregiverName(appointment.getCaregiver().getFullName())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .serviceType(appointment.getServiceType())
                .shiftType(appointment.getShiftType())
                .status(appointment.getStatus())
                .evvRequired(appointment.getEvvRequired())
                .billable(appointment.getBillable())
                .notes(appointment.getNotes())
                .completed(appointment.getCompleted())
                .build();
    }
    public AppointmentResponse updateAppointmentStatus(
            Long appointmentId,
            AppointmentStatusUpdateRequest request
    ) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        String status = request.getStatus() != null
                ? request.getStatus().toUpperCase()
                : appointment.getStatus();

        appointment.setStatus(status);

        if ("COMPLETED".equals(status)) {
            appointment.setCompleted(true);
        } else {
            appointment.setCompleted(false);
        }

        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            appointment.setNotes(request.getNotes());
        }

        return mapToResponse(appointmentRepository.save(appointment));
    }
}