package com.homecare.service;

import com.homecare.dto.TimesheetResponse;
import com.homecare.dto.TimesheetReviewRequest;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final ClientAuthorizationRepository authorizationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public TimesheetService(
            TimesheetRepository timesheetRepository,
            ClockRecordRepository clockRecordRepository,
            ServiceDocumentationRepository serviceDocumentationRepository,
            ClientAuthorizationRepository authorizationRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.timesheetRepository = timesheetRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.authorizationRepository = authorizationRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public TimesheetResponse generateFromClockRecord(Long clockRecordId) {
        if (timesheetRepository.existsByClockRecordId(clockRecordId)) {

            Timesheet existingTimesheet = timesheetRepository
                    .findByClockRecordId(clockRecordId)
                    .orElseThrow(() -> new RuntimeException("Timesheet not found."));

            auditLogService.logAction(
                    existingTimesheet.getCaregiver().getId(),
                    existingTimesheet.getCaregiver().getFullName(),
                    existingTimesheet.getCaregiver().getRole().name(),
                    existingTimesheet.getClient().getId(),
                    "VIEW_EXISTING_TIMESHEET",
                    "TIMESHEET",
                    existingTimesheet.getId(),
                    "Existing timesheet returned for clock record "
                            + clockRecordId
                            + "."
            );

            return mapToResponse(existingTimesheet);
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

        boolean authorizationValid = false;

        if (authorization != null) {
            double approvedTotal = authorization.getApprovedTotalHours() != null
                    ? authorization.getApprovedTotalHours()
                    : 0.0;

            double alreadyUsed = timesheetRepository.findByAuthorizationId(authorization.getId())
                    .stream()
                    .filter(existing -> existing.getTotalHours() != null)
                    .mapToDouble(Timesheet::getTotalHours)
                    .sum();

            double remaining = approvedTotal - alreadyUsed;

            authorizationValid = remaining >= totalHours;
        }

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
                .billingStatus(authorizationValid ? "PENDING" : "NEEDS_REVIEW")
                .documentationApproved(documentationApproved)
                .authorizationValid(authorizationValid)
                .billable(documentationApproved && authorizationValid)
                .authorizationOverride(false)
                .notes(null)
                .build();

        Timesheet savedTimesheet = timesheetRepository.save(timesheet);

        auditLogService.logAction(
                caregiver.getId(),
                caregiver.getFullName(),
                caregiver.getRole().name(),
                client.getId(),
                "GENERATE_TIMESHEET",
                "TIMESHEET",
                savedTimesheet.getId(),
                "Timesheet generated from clock record."
        );

        if (!documentationApproved) {
            auditLogService.logAction(
                    caregiver.getId(),
                    caregiver.getFullName(),
                    caregiver.getRole().name(),
                    client.getId(),
                    "TIMESHEET_DOCUMENTATION_NOT_APPROVED",
                    "TIMESHEET",
                    savedTimesheet.getId(),
                    "Timesheet generated but service documentation is not approved."
            );
        }

        if (!authorizationValid) {
            auditLogService.logAction(
                    caregiver.getId(),
                    caregiver.getFullName(),
                    caregiver.getRole().name(),
                    client.getId(),
                    "TIMESHEET_AUTHORIZATION_NEEDS_REVIEW",
                    "TIMESHEET",
                    savedTimesheet.getId(),
                    "Timesheet generated but authorization is missing or insufficient."
            );
        }

        return mapToResponse(savedTimesheet);
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

        User actor = getActor(request.getActorUserId());

        double payRate = request.getCaregiverPayRate() != null
                ? request.getCaregiverPayRate()
                : 0.0;

        double billingRate = request.getBillingRate() != null
                ? request.getBillingRate()
                : 0.0;

        String previousPayrollStatus = timesheet.getPayrollStatus();
        String previousBillingStatus = timesheet.getBillingStatus();
        Boolean previousBillable = timesheet.getBillable();

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

        boolean wantsBillable = Boolean.TRUE.equals(request.getBillable());

        if (Boolean.FALSE.equals(timesheet.getAuthorizationValid()) && wantsBillable) {
            if (request.getAuthorizationOverrideReason() == null ||
                    request.getAuthorizationOverrideReason().isBlank()) {
                throw new RuntimeException(
                        "Authorization override reason is required when approving an overused authorization."
                );
            }

            timesheet.setAuthorizationOverride(true);
            timesheet.setAuthorizationOverrideReason(request.getAuthorizationOverrideReason());
            timesheet.setAuthorizationOverrideAt(LocalDateTime.now());
        }

        Timesheet savedTimesheet = timesheetRepository.save(timesheet);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedTimesheet.getClient().getId(),
                "REVIEW_TIMESHEET",
                "TIMESHEET",
                savedTimesheet.getId(),
                "Timesheet reviewed."
        );

        if (!equalsIgnoreCase(previousPayrollStatus, savedTimesheet.getPayrollStatus())) {
            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    savedTimesheet.getClient().getId(),
                    getPayrollAuditAction(savedTimesheet.getPayrollStatus()),
                    "TIMESHEET",
                    savedTimesheet.getId(),
                    "Payroll status changed from "
                            + previousPayrollStatus
                            + " to "
                            + savedTimesheet.getPayrollStatus()
                            + "."
            );
        }

        if (!equalsIgnoreCase(previousBillingStatus, savedTimesheet.getBillingStatus())) {
            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    savedTimesheet.getClient().getId(),
                    getBillingAuditAction(savedTimesheet.getBillingStatus()),
                    "TIMESHEET",
                    savedTimesheet.getId(),
                    "Billing status changed from "
                            + previousBillingStatus
                            + " to "
                            + savedTimesheet.getBillingStatus()
                            + "."
            );
        }

        if (previousBillable == null ||
                !previousBillable.equals(savedTimesheet.getBillable())) {
            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    savedTimesheet.getClient().getId(),
                    "UPDATE_TIMESHEET_BILLABLE_STATUS",
                    "TIMESHEET",
                    savedTimesheet.getId(),
                    "Timesheet billable status changed to "
                            + savedTimesheet.getBillable()
                            + "."
            );
        }

        if (Boolean.TRUE.equals(savedTimesheet.getAuthorizationOverride())) {
            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    savedTimesheet.getClient().getId(),
                    "AUTHORIZATION_OVERRIDE",
                    "TIMESHEET",
                    savedTimesheet.getId(),
                    savedTimesheet.getAuthorizationOverrideReason()
            );
        }

        return mapToResponse(savedTimesheet);
    }

    private ClientAuthorization findActiveAuthorization(Long clientId) {
        LocalDate today = LocalDate.now();

        return authorizationRepository
                .findByClientIdAndStatusOrderByEndDateDesc(clientId, "ACTIVE")
                .stream()
                .filter(auth ->
                        auth.getStartDate() != null &&
                                auth.getEndDate() != null &&
                                !auth.getStartDate().isAfter(today) &&
                                !auth.getEndDate().isBefore(today)
                )
                .findFirst()
                .orElse(null);
    }

    private double calculateHours(LocalDateTime start, LocalDateTime end) {

        long seconds = Duration.between(start, end).getSeconds();

        if (seconds <= 0) {
            throw new RuntimeException("Invalid clock times.");
        }

        return Math.round((seconds / 3600.0) * 100.0) / 100.0;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private User getActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new RuntimeException("Actor user is required for audit logging.");
        }

        return userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("Actor user not found."));
    }

    private boolean equalsIgnoreCase(String first, String second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equalsIgnoreCase(second);
    }

    private String getPayrollAuditAction(String status) {
        if (status == null) {
            return "UPDATE_PAYROLL_STATUS";
        }

        return switch (status.toUpperCase()) {
            case "APPROVED" -> "APPROVE_PAYROLL";
            case "PAID" -> "MARK_PAYROLL_PAID";
            case "REJECTED", "DENIED" -> "REJECT_PAYROLL";
            default -> "UPDATE_PAYROLL_STATUS";
        };
    }

    private String getBillingAuditAction(String status) {
        if (status == null) {
            return "UPDATE_BILLING_STATUS";
        }

        return switch (status.toUpperCase()) {
            case "APPROVED" -> "APPROVE_BILLING";
            case "BILLED" -> "MARK_BILLED";
            case "DENIED", "REJECTED" -> "REJECT_BILLING";
            case "NEEDS_REVIEW" -> "BILLING_NEEDS_REVIEW";
            default -> "UPDATE_BILLING_STATUS";
        };
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
                .authorizationOverride(timesheet.getAuthorizationOverride())
                .authorizationOverrideReason(timesheet.getAuthorizationOverrideReason())
                .authorizationOverrideAt(timesheet.getAuthorizationOverrideAt())
                .build();
    }
}