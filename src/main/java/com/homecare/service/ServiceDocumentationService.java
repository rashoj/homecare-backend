package com.homecare.service;

import com.homecare.dto.ServiceDocumentationRequest;
import com.homecare.dto.ServiceDocumentationResponse;
import com.homecare.dto.ServiceDocumentationReviewRequest;
import com.homecare.entity.Appointment;
import com.homecare.entity.ServiceDocumentation;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.ServiceDocumentationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.homecare.entity.ClockRecord;
import com.homecare.repository.ClockRecordRepository;

@Service
public class ServiceDocumentationService {

    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final TimesheetService timesheetService;

    public ServiceDocumentationService(
            ServiceDocumentationRepository serviceDocumentationRepository,
            AppointmentRepository appointmentRepository,
            ClockRecordRepository clockRecordRepository,
            TimesheetService timesheetService
    ) {
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.appointmentRepository = appointmentRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.timesheetService = timesheetService;
    }

    public ServiceDocumentationResponse submitDocumentation(ServiceDocumentationRequest request) {
        if (serviceDocumentationRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Service documentation already exists for this appointment.");
        }

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (!Boolean.TRUE.equals(request.getShiftCompleted())) {
            throw new RuntimeException("Shift completion verification is required.");
        }

        if (request.getCaregiverSignature() == null || request.getCaregiverSignature().isBlank()) {
            throw new RuntimeException("Caregiver signature is required.");
        }

        ServiceDocumentation documentation = ServiceDocumentation.builder()
                .appointment(appointment)
                .client(appointment.getClient())
                .caregiver(appointment.getCaregiver())
                .shiftTasksCompleted(request.getShiftTasksCompleted())
                .adlsCompleted(request.getAdlsCompleted())
                .goalProgressNotes(request.getGoalProgressNotes())
                .dailyServiceNotes(request.getDailyServiceNotes())
                .shiftCompleted(request.getShiftCompleted())
                .caregiverSignature(request.getCaregiverSignature())
                .status("SUBMITTED")
                .locked(false)
                .submittedAt(LocalDateTime.now())
                .build();

        return mapToResponse(serviceDocumentationRepository.save(documentation));
    }

    public ServiceDocumentationResponse reviewDocumentation(
            Long id,
            ServiceDocumentationReviewRequest request
    ) {
        ServiceDocumentation documentation = serviceDocumentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service documentation not found."));

        if (Boolean.TRUE.equals(documentation.getLocked())) {
            throw new RuntimeException("Finalized documentation is locked and cannot be changed.");
        }

        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
            documentation.setApprovedAt(LocalDateTime.now());
            documentation.setLocked(true);

            ClockRecord clockRecord = clockRecordRepository
                    .findByAppointmentId(documentation.getAppointment().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Cannot generate billing record. Clock record not found for this appointment."
                    ));

            timesheetService.generateFromClockRecord(clockRecord.getId());
        }
        documentation.setStatus(request.getStatus().toUpperCase());
        documentation.setSupervisorComments(request.getSupervisorComments());

        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
            documentation.setApprovedAt(LocalDateTime.now());
            documentation.setLocked(true);
        }

        return mapToResponse(serviceDocumentationRepository.save(documentation));
    }

    public List<ServiceDocumentationResponse> getDocumentationByClient(Long clientId) {
        return serviceDocumentationRepository.findByClientIdOrderBySubmittedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ServiceDocumentationResponse> getPendingDocumentation() {
        return serviceDocumentationRepository.findByStatusOrderBySubmittedAtDesc("SUBMITTED")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ServiceDocumentationResponse getDocumentationByAppointment(Long appointmentId) {
        ServiceDocumentation documentation = serviceDocumentationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Service documentation not found."));

        return mapToResponse(documentation);
    }

    private ServiceDocumentationResponse mapToResponse(ServiceDocumentation documentation) {
        return ServiceDocumentationResponse.builder()
                .id(documentation.getId())
                .appointmentId(documentation.getAppointment().getId())
                .clientId(documentation.getClient().getId())
                .clientName(documentation.getClient().getFullName())
                .caregiverId(documentation.getCaregiver().getId())
                .caregiverName(documentation.getCaregiver().getFullName())
                .shiftTasksCompleted(documentation.getShiftTasksCompleted())
                .adlsCompleted(documentation.getAdlsCompleted())
                .goalProgressNotes(documentation.getGoalProgressNotes())
                .dailyServiceNotes(documentation.getDailyServiceNotes())
                .shiftCompleted(documentation.getShiftCompleted())
                .caregiverSignature(documentation.getCaregiverSignature())
                .status(documentation.getStatus())
                .locked(documentation.getLocked())
                .supervisorComments(documentation.getSupervisorComments())
                .submittedAt(documentation.getSubmittedAt())
                .approvedAt(documentation.getApprovedAt())
                .build();
    }
}