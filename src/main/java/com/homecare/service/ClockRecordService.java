package com.homecare.service;

import com.homecare.dto.ClockInRequest;
import com.homecare.dto.ClockOutRequest;
import com.homecare.dto.ClockRecordResponse;
import com.homecare.entity.Appointment;
import com.homecare.entity.ClockRecord;
import com.homecare.entity.EVVException;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.ClockRecordRepository;
import com.homecare.repository.EVVAlertRepository;
import com.homecare.repository.EVVExceptionRepository;
import org.springframework.stereotype.Service;
import com.homecare.service.EVVAlertService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClockRecordService {

    private final ClockRecordRepository clockRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final EVVExceptionRepository evvExceptionRepository;
    private final EVVAlertService evvAlertService;

    public ClockRecordService(
            ClockRecordRepository clockRecordRepository,
            AppointmentRepository appointmentRepository,
            EVVExceptionRepository evvExceptionRepository,
            EVVAlertService evvAlertService
    ) {
        this.clockRecordRepository = clockRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.evvExceptionRepository = evvExceptionRepository;
        this.evvAlertService = evvAlertService;
    }

    public ClockRecordResponse clockIn(ClockInRequest request) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (clockRecordRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Already clocked in for this appointment");
        }

        ClockRecord clockRecord = ClockRecord.builder()
                .appointment(appointment)
                .clockInTime(LocalDateTime.now())
                .clockInLatitude(request.getLatitude())
                .clockInLongitude(request.getLongitude())
                .clockInNotes(request.getNotes())
                .status("CLOCKED_IN")
                .build();

        ClockRecord savedClockRecord = clockRecordRepository.save(clockRecord);

        createClockInExceptionsIfNeeded(appointment, savedClockRecord, request);

        appointment.setStatus("IN_PROGRESS");
        appointment.setCompleted(false);
        appointmentRepository.save(appointment);

        return mapToResponse(savedClockRecord);
    }

    public ClockRecordResponse clockOut(ClockOutRequest request) {

        ClockRecord clockRecord = clockRecordRepository.findByAppointmentId(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Clock-in record not found"));

        if (clockRecord.getClockOutTime() != null) {
            throw new RuntimeException("Already clocked out for this appointment");
        }

        clockRecord.setClockOutTime(LocalDateTime.now());
        clockRecord.setClockOutLatitude(request.getLatitude());
        clockRecord.setClockOutLongitude(request.getLongitude());
        clockRecord.setClockOutNotes(request.getNotes());
        clockRecord.setStatus("CLOCKED_OUT");

        long minutes = Duration.between(clockRecord.getClockInTime(), clockRecord.getClockOutTime()).toMinutes();
        clockRecord.setTotalHours(minutes / 60.0);

        ClockRecord savedClockRecord = clockRecordRepository.save(clockRecord);

        Appointment appointment = savedClockRecord.getAppointment();

        createClockOutExceptionsIfNeeded(appointment, savedClockRecord, request);

        appointment.setStatus("COMPLETED");
        appointment.setCompleted(true);
        appointmentRepository.save(appointment);

        return mapToResponse(savedClockRecord);
    }

    public List<ClockRecordResponse> getAllClockRecords() {
        return clockRecordRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClockRecordResponse getClockRecordByAppointment(Long appointmentId) {
        ClockRecord clockRecord = clockRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Clock record not found"));

        return mapToResponse(clockRecord);
    }

    public List<ClockRecordResponse> getClockRecordsByClient(Long clientId) {
        return clockRecordRepository.findByAppointmentClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void createClockInExceptionsIfNeeded(
            Appointment appointment,
            ClockRecord clockRecord,
            ClockInRequest request
    ) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            createException(
                    appointment,
                    clockRecord,
                    "GPS_MISSING",
                    "HIGH",
                    "Caregiver clocked in without GPS coordinates."
            );
        }

        if (appointment.getStartTime() != null &&
                clockRecord.getClockInTime().isAfter(appointment.getStartTime().plusMinutes(15))) {
            createException(
                    appointment,
                    clockRecord,
                    "LATE_CLOCK_IN",
                    "MEDIUM",
                    "Caregiver clocked in more than 15 minutes after scheduled start time."
            );
        }
    }

    private void createClockOutExceptionsIfNeeded(
            Appointment appointment,
            ClockRecord clockRecord,
            ClockOutRequest request
    ) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            createException(
                    appointment,
                    clockRecord,
                    "GPS_MISSING",
                    "HIGH",
                    "Caregiver clocked out without GPS coordinates."
            );

        }

        if (appointment.getEndTime() != null &&
                clockRecord.getClockOutTime().isBefore(appointment.getEndTime().minusMinutes(15))) {
            createException(
                    appointment,
                    clockRecord,
                    "EARLY_CLOCK_OUT",
                    "MEDIUM",
                    "Caregiver clocked out more than 15 minutes before scheduled end time."
            );
            long minutesWorked = Duration.between(
                    clockRecord.getClockInTime(),
                    clockRecord.getClockOutTime()
            ).toMinutes();

            if (minutesWorked < 15) {
                createException(
                        appointment,
                        clockRecord,
                        "SHORT_VISIT",
                        "HIGH",
                        "Visit duration was less than 15 minutes."
                );
            }
        }
    }

    private void createException(
            Appointment appointment,
            ClockRecord clockRecord,
            String exceptionType,
            String severity,
            String description
    ) {
        boolean alreadyExists = evvExceptionRepository
                .existsByAppointmentIdAndClockRecordIdAndExceptionType(
                        appointment.getId(),
                        clockRecord.getId(),
                        exceptionType
                );

        if (alreadyExists) {
            return;
        }

        EVVException exception = EVVException.builder()
                .appointment(appointment)
                .clockRecord(clockRecord)
                .client(appointment.getClient())
                .caregiver(appointment.getCaregiver())
                .exceptionType(exceptionType)
                .severity(severity)
                .status("OPEN")
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        EVVException savedException = evvExceptionRepository.save(exception);

        evvAlertService.createAlertFromException(savedException);
    }

    private ClockRecordResponse mapToResponse(ClockRecord clockRecord) {

        Appointment appointment = clockRecord.getAppointment();

        return ClockRecordResponse.builder()
                .id(clockRecord.getId())
                .appointmentId(appointment.getId())
                .clientName(appointment.getClient().getFullName())
                .caregiverName(appointment.getCaregiver().getFullName())
                .clockInTime(clockRecord.getClockInTime())
                .clockOutTime(clockRecord.getClockOutTime())
                .clockInLatitude(clockRecord.getClockInLatitude())
                .clockInLongitude(clockRecord.getClockInLongitude())
                .clockOutLatitude(clockRecord.getClockOutLatitude())
                .clockOutLongitude(clockRecord.getClockOutLongitude())
                .totalHours(clockRecord.getTotalHours())
                .status(clockRecord.getStatus())
                .clockInNotes(clockRecord.getClockInNotes())
                .clockOutNotes(clockRecord.getClockOutNotes())
                .build();
    }
}