package com.homecare.service;

import com.homecare.dto.ClockInRequest;
import com.homecare.dto.ClockOutRequest;
import com.homecare.dto.ClockRecordResponse;
import com.homecare.entity.Appointment;
import com.homecare.entity.ClockRecord;
import com.homecare.entity.EVVException;
import com.homecare.entity.User;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;
import com.homecare.dto.ClockRecordAdjustmentRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClockRecordService {

    private final ClockRecordRepository clockRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final EVVExceptionRepository evvExceptionRepository;
    private final EVVAlertService evvAlertService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final TimesheetRepository timesheetRepository;
    private final TimesheetService timesheetService;

    public ClockRecordService(
            ClockRecordRepository clockRecordRepository,
            AppointmentRepository appointmentRepository,
            EVVExceptionRepository evvExceptionRepository,
            EVVAlertService evvAlertService,
            UserRepository userRepository,
            AuditLogService auditLogService,
            TimesheetRepository timesheetRepository,
            TimesheetService timesheetService
    ) {
        this.clockRecordRepository = clockRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.evvExceptionRepository = evvExceptionRepository;
        this.evvAlertService = evvAlertService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.timesheetRepository = timesheetRepository;
        this.timesheetService = timesheetService;
    }

    public ClockRecordResponse clockIn(ClockInRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new RuntimeException("Actor user not found."));

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

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                appointment.getClient().getId(),
                "CLOCK_IN",
                "CLOCK_RECORD",
                savedClockRecord.getId(),
                "Caregiver clocked in."
        );

        createClockInExceptionsIfNeeded(appointment, savedClockRecord, request);

        appointment.setStatus("IN_PROGRESS");
        appointment.setCompleted(false);
        appointmentRepository.save(appointment);

        return mapToResponse(savedClockRecord);
    }

    public ClockRecordResponse clockOut(ClockOutRequest request) {
        ClockRecord clockRecord = clockRecordRepository.findByAppointmentId(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Clock-in record not found"));

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new RuntimeException("Actor user not found."));

        if (clockRecord.getClockOutTime() != null) {
            throw new RuntimeException("Already clocked out for this appointment");
        }

        clockRecord.setClockOutTime(LocalDateTime.now());
        clockRecord.setClockOutLatitude(request.getLatitude());
        clockRecord.setClockOutLongitude(request.getLongitude());
        clockRecord.setClockOutNotes(request.getNotes());
        clockRecord.setStatus("CLOCKED_OUT");

        long minutes = Duration.between(
                clockRecord.getClockInTime(),
                clockRecord.getClockOutTime()
        ).toMinutes();

        clockRecord.setTotalHours(minutes / 60.0);

        ClockRecord savedClockRecord = clockRecordRepository.save(clockRecord);
        Appointment appointment = savedClockRecord.getAppointment();

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                appointment.getClient().getId(),
                "CLOCK_OUT",
                "CLOCK_RECORD",
                savedClockRecord.getId(),
                "Caregiver clocked out."
        );

        createClockOutExceptionsIfNeeded(appointment, savedClockRecord, request);

        appointment.setStatus("COMPLETED");
        appointment.setCompleted(true);
        appointmentRepository.save(appointment);

        try {
            timesheetService.generateFromClockRecord(savedClockRecord.getId());
        } catch (Exception e) {
            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    appointment.getClient().getId(),
                    "TIMESHEET_AUTO_GENERATION_FAILED",
                    "CLOCK_RECORD",
                    savedClockRecord.getId(),
                    e.getMessage()
            );
        }

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
        }

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
                .organization(appointment.getOrganization())
                .exceptionType(exceptionType)
                .severity(severity)
                .status("OPEN")
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        EVVException savedException = evvExceptionRepository.save(exception);

        auditLogService.logAction(
                appointment.getCaregiver().getId(),
                appointment.getCaregiver().getFullName(),
                appointment.getCaregiver().getRole().name(),
                appointment.getClient().getId(),
                "EVV_EXCEPTION_CREATED",
                "EVV_EXCEPTION",
                savedException.getId(),
                exceptionType + " exception created."
        );

        evvAlertService.createAlertFromException(savedException);
    }

    private ClockRecordResponse mapToResponse(ClockRecord clockRecord) {

        Appointment appointment = clockRecord.getAppointment();

        return ClockRecordResponse.builder()
                .id(clockRecord.getId())
                .appointmentId(appointment != null ? appointment.getId() : null)
                .clientName(
                        appointment != null && appointment.getClient() != null
                                ? appointment.getClient().getFullName()
                                : null
                )
                .caregiverName(
                        appointment != null && appointment.getCaregiver() != null
                                ? appointment.getCaregiver().getFullName()
                                : null
                )
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
    public ClockRecordResponse adminAdjustClockRecord(
            Long id,
            ClockRecordAdjustmentRequest request
    ) {
        if (request.getActorUserId() == null) {
            throw new RuntimeException("Actor user is required for audit logging.");
        }

        if (request.getAdjustmentReason() == null ||
                request.getAdjustmentReason().isBlank()) {
            throw new RuntimeException("Adjustment reason is required.");
        }

        if (request.getClockInTime() == null || request.getClockOutTime() == null) {
            throw new RuntimeException("Clock in and clock out times are required.");
        }

        if (!request.getClockOutTime().isAfter(request.getClockInTime())) {
            throw new RuntimeException("Clock out time must be after clock in time.");
        }

        User actor = userRepository.findById(request.getActorUserId())
                .orElseThrow(() -> new RuntimeException("Actor user not found."));

        ClockRecord clockRecord = clockRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clock record not found."));

        clockRecord.setClockInTime(request.getClockInTime());
        clockRecord.setClockOutTime(request.getClockOutTime());
        clockRecord.setStatus("CLOCKED_OUT");

        ClockRecord savedClockRecord = clockRecordRepository.save(clockRecord);

        timesheetRepository.findByClockRecordId(savedClockRecord.getId())
                .ifPresent(timesheet -> {
                    double totalHours = calculateHours(
                            savedClockRecord.getClockInTime(),
                            savedClockRecord.getClockOutTime()
                    );

                    timesheet.setClockInTime(savedClockRecord.getClockInTime());
                    timesheet.setClockOutTime(savedClockRecord.getClockOutTime());
                    timesheet.setTotalHours(totalHours);
                    timesheet.setRegularHours(Math.min(totalHours, 8.0));
                    timesheet.setOvertimeHours(Math.max(totalHours - 8.0, 0.0));

                    double payRate = timesheet.getCaregiverPayRate() != null
                            ? timesheet.getCaregiverPayRate()
                            : 0.0;

                    double billingRate = timesheet.getBillingRate() != null
                            ? timesheet.getBillingRate()
                            : 0.0;

                    timesheet.setCaregiverPayAmount(roundMoney(totalHours * payRate));
                    timesheet.setBillableAmount(roundMoney(totalHours * billingRate));

                    timesheetRepository.save(timesheet);
                });

        Appointment appointment = savedClockRecord.getAppointment();

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                appointment.getClient().getId(),
                "ADMIN_ADJUST_CLOCK_RECORD",
                "CLOCK_RECORD",
                savedClockRecord.getId(),
                request.getAdjustmentReason()
        );

        return mapToResponse(savedClockRecord);
    }
    private double calculateHours(LocalDateTime start, LocalDateTime end) {
        long seconds = java.time.Duration.between(start, end).getSeconds();

        if (seconds <= 0) {
            throw new RuntimeException("Invalid clock times.");
        }

        return Math.round((seconds / 3600.0) * 100.0) / 100.0;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}