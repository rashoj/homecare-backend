package com.homecare.service;

import com.homecare.dto.ClientComplianceRowResponse;
import com.homecare.dto.ComplianceSummaryResponse;
import com.homecare.dto.MissedMedicationAlertResponse;
import com.homecare.dto.MissingVisitNoteAlertResponse;
import com.homecare.entity.Appointment;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplianceService {

    private final MedicationLogRepository medicationLogRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;

    public ComplianceService(
            MedicationLogRepository medicationLogRepository,
            VisitNoteRepository visitNoteRepository,
            ClockRecordRepository clockRecordRepository,
            AppointmentRepository appointmentRepository,
            ClientRepository clientRepository
    ) {
        this.medicationLogRepository = medicationLogRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
    }

    public ComplianceSummaryResponse getSummary() {

        long missedMedications =
                medicationLogRepository.countByStatus("MISSED");

        long openClockRecords =
                clockRecordRepository.countByStatus("CLOCKED_IN");

        long incidentsReported =
                visitNoteRepository.countByIncidentReportedTrue();

        List<Appointment> completedAppointments =
                appointmentRepository.findByCompletedTrue();

        long visitsMissingNotes = completedAppointments
                .stream()
                .filter(appointment ->
                        !visitNoteRepository.existsByAppointmentId(appointment.getId())
                )
                .count();

        return ComplianceSummaryResponse.builder()
                .missedMedications(missedMedications)
                .visitsMissingNotes(visitsMissingNotes)
                .openClockRecords(openClockRecords)
                .incidentsReported(incidentsReported)
                .build();
    }

    public List<ClientComplianceRowResponse> getClientComplianceRows() {

        return clientRepository.findByActiveTrue()
                .stream()
                .map(client -> {

                    Long clientId = client.getId();

                    long missedMedications =
                            medicationLogRepository.countByClientIdAndStatus(
                                    clientId,
                                    "MISSED"
                            );

                    long refusedMedications =
                            medicationLogRepository.countByClientIdAndStatus(
                                    clientId,
                                    "REFUSED"
                            );

                    long openClockRecords =
                            clockRecordRepository.countByAppointmentClientIdAndStatus(
                                    clientId,
                                    "CLOCKED_IN"
                            );

                    long incidentsReported =
                            visitNoteRepository.countByClientIdAndIncidentReportedTrue(
                                    clientId
                            );

                    long visitsMissingNotes =
                            appointmentRepository.findByClientIdAndCompletedTrue(clientId)
                                    .stream()
                                    .filter(appointment ->
                                            !visitNoteRepository.existsByAppointmentId(
                                                    appointment.getId()
                                            )
                                    )
                                    .count();

                    long totalIssues =
                            missedMedications +
                                    refusedMedications +
                                    visitsMissingNotes +
                                    openClockRecords +
                                    incidentsReported;

                    String status = "COMPLIANT";

                    if (totalIssues > 0) {
                        status = "NEEDS_REVIEW";
                    }

                    if (
                            missedMedications > 0 ||
                                    incidentsReported > 0 ||
                                    openClockRecords > 0
                    ) {
                        status = "CRITICAL";
                    }

                    return ClientComplianceRowResponse.builder()
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .missedMedications(missedMedications)
                            .refusedMedications(refusedMedications)
                            .visitsMissingNotes(visitsMissingNotes)
                            .openClockRecords(openClockRecords)
                            .incidentsReported(incidentsReported)
                            .complianceStatus(status)
                            .build();
                })
                .toList();
    }

    public List<MissedMedicationAlertResponse> getMissedMedicationAlerts() {
        return medicationLogRepository.findByStatusOrderByCreatedAtDesc("MISSED")
                .stream()
                .map(log -> MissedMedicationAlertResponse.builder()
                        .logId(log.getId())
                        .clientId(log.getClient().getId())
                        .clientName(log.getClient().getFullName())
                        .medicationId(log.getMedication().getId())
                        .medicationName(log.getMedication().getMedicationName())
                        .caregiverId(log.getCaregiver() != null ? log.getCaregiver().getId() : null)
                        .caregiverName(log.getCaregiver() != null ? log.getCaregiver().getFullName() : null)
                        .scheduledAt(log.getScheduledAt())
                        .givenAt(log.getGivenAt())
                        .status(log.getStatus())
                        .missedReason(log.getMissedReason())
                        .notes(log.getNotes())
                        .build())
                .toList();
    }
    public List<MissingVisitNoteAlertResponse> getMissingVisitNoteAlerts() {
        return appointmentRepository.findByCompletedTrue()
                .stream()
                .filter(appointment ->
                        !visitNoteRepository.existsByAppointmentId(appointment.getId())
                )
                .map(appointment -> MissingVisitNoteAlertResponse.builder()
                        .appointmentId(appointment.getId())
                        .clientId(appointment.getClient().getId())
                        .clientName(appointment.getClient().getFullName())
                        .caregiverId(appointment.getCaregiver() != null ? appointment.getCaregiver().getId() : null)
                        .caregiverName(appointment.getCaregiver() != null ? appointment.getCaregiver().getFullName() : null)
                        .startTime(appointment.getStartTime())
                        .endTime(appointment.getEndTime())
                        .status(appointment.getStatus())
                        .completed(appointment.getCompleted())
                        .build()
                )
                .toList();
    }
}