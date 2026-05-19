package com.homecare.service;

import com.homecare.dto.ClientRiskRowResponse;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskService {

    private final ClientRepository clientRepository;
    private final IncidentRepository incidentRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final AppointmentRepository appointmentRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final ClockRecordRepository clockRecordRepository;

    public RiskService(
            ClientRepository clientRepository,
            IncidentRepository incidentRepository,
            MedicationLogRepository medicationLogRepository,
            AppointmentRepository appointmentRepository,
            VisitNoteRepository visitNoteRepository,
            ClockRecordRepository clockRecordRepository
    ) {
        this.clientRepository = clientRepository;
        this.incidentRepository = incidentRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.appointmentRepository = appointmentRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.clockRecordRepository = clockRecordRepository;
    }

    public List<ClientRiskRowResponse> getClientRiskRows() {
        return clientRepository.findByActiveTrue()
                .stream()
                .map(client -> {
                    Long clientId = client.getId();

                    long totalIncidents =
                            incidentRepository.countByClientId(clientId);

                    long highSeverityIncidents =
                            incidentRepository.countByClientIdAndSeverity(clientId, "HIGH");

                    long criticalIncidents =
                            incidentRepository.countByClientIdAndSeverity(clientId, "CRITICAL");

                    long stateReportableIncidents =
                            incidentRepository.countByClientIdAndStateReportableTrue(clientId);

                    long missedMedications =
                            medicationLogRepository.countByClientIdAndStatus(clientId, "MISSED");

                    long openClockRecords =
                            clockRecordRepository.countByAppointmentClientIdAndStatus(
                                    clientId,
                                    "CLOCKED_IN"
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

                    long riskScore =
                            (criticalIncidents * 30) +
                                    (highSeverityIncidents * 20) +
                                    (stateReportableIncidents * 25) +
                                    (missedMedications * 15) +
                                    (openClockRecords * 10) +
                                    (visitsMissingNotes * 5) +
                                    (totalIncidents * 3);

                    String riskLevel = "LOW";

                    if (riskScore >= 70) {
                        riskLevel = "CRITICAL";
                    } else if (riskScore >= 40) {
                        riskLevel = "HIGH";
                    } else if (riskScore >= 15) {
                        riskLevel = "MEDIUM";
                    }

                    return ClientRiskRowResponse.builder()
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .totalIncidents(totalIncidents)
                            .highSeverityIncidents(highSeverityIncidents)
                            .criticalIncidents(criticalIncidents)
                            .stateReportableIncidents(stateReportableIncidents)
                            .missedMedications(missedMedications)
                            .visitsMissingNotes(visitsMissingNotes)
                            .openClockRecords(openClockRecords)
                            .riskScore(riskScore)
                            .riskLevel(riskLevel)
                            .build();
                })
                .toList();
    }
}