package com.homecare.service;

import com.homecare.entity.Appointment;
import com.homecare.entity.ClockRecord;
import com.homecare.repository.ClockRecordRepository;
import com.homecare.repository.FraudAlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionService {

    private final ClockRecordRepository clockRecordRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudAlertService fraudAlertService;

    public FraudDetectionService(
            ClockRecordRepository clockRecordRepository,
            FraudAlertRepository fraudAlertRepository,
            FraudAlertService fraudAlertService
    ) {
        this.clockRecordRepository = clockRecordRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.fraudAlertService = fraudAlertService;
    }

    public void detectMissingClockOuts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);

        List<ClockRecord> records =
                clockRecordRepository.findByClockInTimeBeforeAndClockOutTimeIsNull(cutoff);

        for (ClockRecord record : records) {
            Appointment appointment = record.getAppointment();

            if (appointment == null || appointment.getOrganization() == null) {
                continue;
            }

            boolean alreadyExists =
                    fraudAlertRepository.existsByOrganizationIdAndAlertTypeAndVisitIdAndStatus(
                            appointment.getOrganization().getId(),
                            "MISSING_CLOCK_OUT",
                            appointment.getId(),
                            "OPEN"
                    );

            if (alreadyExists) {
                continue;
            }

            fraudAlertService.createAlert(
                    appointment.getOrganization(),
                    appointment.getCaregiver(),
                    appointment.getClient(),
                    appointment.getId(),
                    "MISSING_CLOCK_OUT",
                    "HIGH",
                    30,
                    "Missing Clock Out",
                    "Caregiver clocked in but did not clock out after 12 hours."
            );
        }
    }
}