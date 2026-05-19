package com.homecare.service;

import com.homecare.dto.TimesheetResponse;
import com.homecare.dto.TimesheetReviewRequest;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final ClientAuthorizationRepository authorizationRepository;

    public TimesheetService(
            TimesheetRepository timesheetRepository,
            ClockRecordRepository clockRecordRepository,
            ServiceDocumentationRepository serviceDocumentationRepository,
            ClientAuthorizationRepository authorizationRepository
    ) {
        this.timesheetRepository = timesheetRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.authorizationRepository = authorizationRepository;
    }

    public TimesheetResponse generateFromClockRecord(Long clockRecordId) {
        if (timesheetRepository.existsByClockRecordId(clockRecordId)) {
            return mapToResponse(
                    timesheetRepository.findByClockRecordId(clockRecordId)
                            .orElseThrow(() -> new RuntimeException("Timesheet not found."))
            );
        }

        ClockRecord clockRecord = clockRecordRepository.findById(clockRecordId)
                .orElseThrow(() -> new RuntimeException("Clock record not found."));

        if (clockRecord.getClockOutTime() == null) {
            throw new RuntimeException("Cannot generate timesheet for open clock record.");
        }

        Appointment appointment = clockRecord.getAppointment();

        if (appointment == null) {
            throw new RuntimeException("Clock record is not linked to appointment.");
        }

        Client client = appointment.getClient();
        User caregiver = appointment.getCaregiver();

        double totalHours = calculateHours(
                clockRecord.getClockInTime(),
                clockRecord.getClockOutTime()
        );

        double regularHours = Math.min(totalHours, 8.0);
        double overtimeHours = Math.max(totalHours - 8.0, 0.0);

        boolean documentationApproved =
                serviceDocumentationRepository.findByAppointmentId(appointment.getId())
                        .map(doc ->
                                "APPROVED".equalsIgnoreCase(doc.getStatus())
                                        && Boolean.TRUE.equals(doc.getLocked())
                        )
                        .orElse(false);

        ClientAuthorization authorization = findActiveAuthorization(client.getId());

        boolean authorizationValid = authorization != null;

        Timesheet timesheet = Timesheet.builder()
                .clockRecord(clockRecord)
                .appointment(appointment)
                .client(client)
                .caregiver(caregiver)
                .authorization(authorization)
                .clockInTime(clockRecord.getClockInTime())
                .clockOutTime(clockRecord.getClockOutTime())
                .totalHours(totalHours)
                .regularHours(regularHours)
                .overtimeHours(overtimeHours)
                .caregiverPayRate(0.0)
                .caregiverPayAmount(0.0)
                .billingRate(0.0)
                .billableAmount(0.0)
                .payrollStatus("PENDING")
                .billingStatus("PENDING")
                .documentationApproved(documentationApproved)
                .authorizationValid(authorizationValid)
                .billable(documentationApproved && authorizationValid)
                .notes(null)
                .build();

        return mapToResponse(timesheetRepository.save(timesheet));
    }

    public List<TimesheetResponse> getAllTimesheets() {
        return timesheetRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TimesheetResponse> getTimesheetsByClient(Long clientId) {
        return timesheetRepository.findByClientIdOrderByClockInTimeDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TimesheetResponse> getTimesheetsByCaregiver(Long caregiverId) {
        return timesheetRepository.findByCaregiverIdOrderByClockInTimeDesc(caregiverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TimesheetResponse reviewTimesheet(
            Long id,
            TimesheetReviewRequest request
    ) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found."));

        double payRate = request.getCaregiverPayRate() != null
                ? request.getCaregiverPayRate()
                : 0.0;

        double billingRate = request.getBillingRate() != null
                ? request.getBillingRate()
                : 0.0;

        timesheet.setCaregiverPayRate(payRate);
        timesheet.setBillingRate(billingRate);

        timesheet.setCaregiverPayAmount(
                roundMoney(timesheet.getTotalHours() * payRate)
        );

        timesheet.setBillableAmount(
                roundMoney(timesheet.getTotalHours() * billingRate)
        );

        if (request.getPayrollStatus() != null) {
            timesheet.setPayrollStatus(request.getPayrollStatus().toUpperCase());
        }

        if (request.getBillingStatus() != null) {
            timesheet.setBillingStatus(request.getBillingStatus().toUpperCase());
        }

        if (request.getBillable() != null) {
            timesheet.setBillable(request.getBillable());
        }

        timesheet.setNotes(request.getNotes());
        timesheet.setReviewedAt(LocalDateTime.now());

        return mapToResponse(timesheetRepository.save(timesheet));
    }

    private ClientAuthorization findActiveAuthorization(Long clientId) {
        return authorizationRepository
                .findByClientIdAndStatusOrderByEndDateDesc(clientId, "ACTIVE")
                .stream()
                .filter(auth ->
                        auth.getEndDate() != null &&
                                !auth.getEndDate().isBefore(java.time.LocalDate.now())
                )
                .findFirst()
                .orElse(null);
    }

    private double calculateHours(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();

        if (minutes <= 0) {
            throw new RuntimeException("Invalid clock times.");
        }

        return Math.round((minutes / 60.0) * 100.0) / 100.0;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private TimesheetResponse mapToResponse(Timesheet timesheet) {
        return TimesheetResponse.builder()
                .id(timesheet.getId())
                .clockRecordId(timesheet.getClockRecord().getId())
                .appointmentId(timesheet.getAppointment().getId())
                .clientId(timesheet.getClient().getId())
                .clientName(timesheet.getClient().getFullName())
                .caregiverId(timesheet.getCaregiver().getId())
                .caregiverName(timesheet.getCaregiver().getFullName())
                .authorizationId(timesheet.getAuthorization() != null ? timesheet.getAuthorization().getId() : null)
                .authorizationNumber(timesheet.getAuthorization() != null ? timesheet.getAuthorization().getAuthorizationNumber() : null)
                .clockInTime(timesheet.getClockInTime())
                .clockOutTime(timesheet.getClockOutTime())
                .totalHours(timesheet.getTotalHours())
                .regularHours(timesheet.getRegularHours())
                .overtimeHours(timesheet.getOvertimeHours())
                .caregiverPayRate(timesheet.getCaregiverPayRate())
                .caregiverPayAmount(timesheet.getCaregiverPayAmount())
                .billingRate(timesheet.getBillingRate())
                .billableAmount(timesheet.getBillableAmount())
                .payrollStatus(timesheet.getPayrollStatus())
                .billingStatus(timesheet.getBillingStatus())
                .documentationApproved(timesheet.getDocumentationApproved())
                .authorizationValid(timesheet.getAuthorizationValid())
                .billable(timesheet.getBillable())
                .notes(timesheet.getNotes())
                .reviewedAt(timesheet.getReviewedAt())
                .build();
    }
}