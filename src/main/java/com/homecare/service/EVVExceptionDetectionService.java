package com.homecare.service;

import com.homecare.entity.Appointment;
import com.homecare.entity.ClockRecord;
import com.homecare.entity.EVVException;
import com.homecare.repository.EVVExceptionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class EVVExceptionDetectionService {

    private final EVVExceptionRepository evvExceptionRepository;

    public EVVExceptionDetectionService(EVVExceptionRepository evvExceptionRepository) {
        this.evvExceptionRepository = evvExceptionRepository;
    }

    public void detectExceptions(Appointment appointment, ClockRecord clockRecord) {
        if (appointment == null || clockRecord == null) {
            return;
        }

        checkLateClockIn(appointment, clockRecord);
        checkEarlyClockOut(appointment, clockRecord);
        checkShortVisit(appointment, clockRecord);
    }

    private void checkLateClockIn(Appointment appointment, ClockRecord clockRecord) {
        LocalDateTime scheduledStart = appointment.getStartTime();
        LocalDateTime actualClockIn = clockRecord.getClockInTime();

        if (scheduledStart == null || actualClockIn == null) {
            return;
        }

        if (actualClockIn.isAfter(scheduledStart.plusMinutes(15))) {
            createException(
                    appointment,
                    clockRecord,
                    "LATE_CLOCK_IN",
                    "MEDIUM",
                    "Caregiver clocked in more than 15 minutes after scheduled start time."
            );
        }
    }

    private void checkEarlyClockOut(Appointment appointment, ClockRecord clockRecord) {
        LocalDateTime scheduledEnd = appointment.getEndTime();
        LocalDateTime actualClockOut = clockRecord.getClockOutTime();

        if (scheduledEnd == null || actualClockOut == null) {
            return;
        }

        if (actualClockOut.isBefore(scheduledEnd.minusMinutes(15))) {
            createException(
                    appointment,
                    clockRecord,
                    "EARLY_CLOCK_OUT",
                    "MEDIUM",
                    "Caregiver clocked out more than 15 minutes before scheduled end time."
            );
        }
    }

    private void checkShortVisit(Appointment appointment, ClockRecord clockRecord) {
        LocalDateTime actualClockIn = clockRecord.getClockInTime();
        LocalDateTime actualClockOut = clockRecord.getClockOutTime();

        if (actualClockIn == null || actualClockOut == null) {
            return;
        }

        long minutesWorked = Duration.between(actualClockIn, actualClockOut).toMinutes();

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
            String type,
            String severity,
            String description
    ) {
        boolean alreadyExists = evvExceptionRepository
                .existsByAppointmentIdAndClockRecordIdAndExceptionType(
                        appointment.getId(),
                        clockRecord.getId(),
                        type
                );

        if (alreadyExists) {
            return;
        }

        EVVException exception = EVVException.builder()
                .appointment(appointment)
                .clockRecord(clockRecord)
                .client(appointment.getClient())
                .caregiver(appointment.getCaregiver())
                .exceptionType(type)
                .severity(severity)
                .status("OPEN")
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        evvExceptionRepository.save(exception);
    }
}