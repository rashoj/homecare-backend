package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.Client;
import com.homecare.entity.Medication;
import com.homecare.entity.MedicationLog;
import com.homecare.entity.User;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.MedicationLogRepository;
import com.homecare.repository.MedicationRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public MedicationService(MedicationRepository medicationRepository,
                             MedicationLogRepository medicationLogRepository,
                             ClientRepository clientRepository,
                             UserRepository userRepository) {
        this.medicationRepository = medicationRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public MedicationResponse createMedication(MedicationRequest request) {

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Medication medication = Medication.builder()
                .client(client)
                .medicationName(request.getMedicationName())
                .dosage(request.getDosage())
                .frequency(request.getFrequency())
                .scheduledTime(request.getScheduledTime())
                .instructions(request.getInstructions())
                .active(true)
                .build();

        return mapMedicationToResponse(medicationRepository.save(medication));
    }

    public List<MedicationResponse> getAllMedications() {
        return medicationRepository.findByActiveTrue()
                .stream()
                .map(this::mapMedicationToResponse)
                .toList();
    }

    public List<MedicationResponse> getMedicationsByClient(Long clientId) {
        return medicationRepository.findByClientIdAndActiveTrue(clientId)
                .stream()
                .map(this::mapMedicationToResponse)
                .toList();
    }

    public MedicationResponse updateMedication(Long id, MedicationRequest request) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        medication.setMedicationName(request.getMedicationName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setScheduledTime(request.getScheduledTime());
        medication.setInstructions(request.getInstructions());

        return mapMedicationToResponse(medicationRepository.save(medication));
    }

    public void deleteMedication(Long id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        medication.setActive(false);
        medicationRepository.save(medication);
    }

    public MedicationLogResponse logMedication(MedicationLogRequest request) {

        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        User caregiver = null;

        if (request.getCaregiverId() != null) {
            caregiver = userRepository.findById(request.getCaregiverId())
                    .orElseThrow(() -> new RuntimeException("Caregiver not found"));
        }

        LocalDateTime scheduledAt = null;

        if (request.getScheduledAt() != null && !request.getScheduledAt().isBlank()) {
            scheduledAt = LocalDateTime.parse(request.getScheduledAt());
        }

        if (scheduledAt != null &&
                medicationLogRepository.existsByMedicationIdAndClientIdAndScheduledAt(
                        medication.getId(),
                        medication.getClient().getId(),
                        scheduledAt
                )) {
            throw new RuntimeException("MAR entry already exists for this scheduled medication pass.");
        }

        MedicationLog log = MedicationLog.builder()
                .medication(medication)
                .client(medication.getClient())
                .caregiver(caregiver)
                .scheduledAt(scheduledAt)
                .status(request.getStatus())
                .notes(request.getNotes())
                .prn(Boolean.TRUE.equals(request.getPrn()))
                .prnReason(request.getPrnReason())
                .refusalReason(request.getRefusalReason())
                .missedReason(request.getMissedReason())
                .caregiverSignature(request.getCaregiverSignature())
                .build();

        return mapLogToResponse(medicationLogRepository.save(log));
    }

    public List<MedicationLogResponse> getMedicationLogsByClient(Long clientId) {
        return medicationLogRepository.findByClientId(clientId)
                .stream()
                .map(this::mapLogToResponse)
                .toList();
    }

    public List<MedicationLogResponse> getMedicationLogsByMedication(Long medicationId) {
        return medicationLogRepository.findByMedicationId(medicationId)
                .stream()
                .map(this::mapLogToResponse)
                .toList();
    }

    private MedicationResponse mapMedicationToResponse(Medication medication) {
        return MedicationResponse.builder()
                .id(medication.getId())
                .clientId(medication.getClient().getId())
                .clientName(medication.getClient().getFullName())
                .medicationName(medication.getMedicationName())
                .dosage(medication.getDosage())
                .frequency(medication.getFrequency())
                .scheduledTime(medication.getScheduledTime())
                .instructions(medication.getInstructions())
                .active(medication.getActive())
                .build();
    }

    private MedicationLogResponse mapLogToResponse(MedicationLog log) {
        return MedicationLogResponse.builder()
                .id(log.getId())
                .medicationId(log.getMedication().getId())
                .medicationName(log.getMedication().getMedicationName())
                .clientId(log.getClient().getId())
                .clientName(log.getClient().getFullName())
                .caregiverId(log.getCaregiver() != null ? log.getCaregiver().getId() : null)
                .caregiverName(log.getCaregiver() != null ? log.getCaregiver().getFullName() : null)
                .scheduledAt(log.getScheduledAt())
                .givenAt(log.getGivenAt())
                .status(log.getStatus())
                .notes(log.getNotes())
                .prn(log.getPrn())
                .prnReason(log.getPrnReason())
                .refusalReason(log.getRefusalReason())
                .missedReason(log.getMissedReason())
                .caregiverSignature(log.getCaregiverSignature())
                .build();
    }
}