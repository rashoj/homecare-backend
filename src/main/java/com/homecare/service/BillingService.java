package com.homecare.service;

import com.homecare.dto.BillingRecordRequest;
import com.homecare.dto.BillingRecordResponse;
import com.homecare.entity.*;
import com.homecare.repository.BillingRecordRepository;
import com.homecare.repository.TimesheetRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillingService {

    private final BillingRecordRepository billingRecordRepository;
    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public BillingService(
            BillingRecordRepository billingRecordRepository,
            TimesheetRepository timesheetRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.billingRecordRepository = billingRecordRepository;
        this.timesheetRepository = timesheetRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public BillingRecordResponse createFromTimesheet(BillingRecordRequest request) {
        Timesheet timesheet = timesheetRepository.findById(request.getTimesheetId())
                .orElseThrow(() -> new RuntimeException("Timesheet not found."));

        User actor = getActor(request.getActorUserId());

        if (billingRecordRepository.existsByTimesheetId(timesheet.getId())) {
            BillingRecord existingRecord = billingRecordRepository.findByTimesheetId(timesheet.getId())
                    .orElseThrow(() -> new RuntimeException("Billing record not found."));

            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    existingRecord.getClient().getId(),
                    "VIEW_EXISTING_BILLING_RECORD",
                    "BILLING_RECORD",
                    existingRecord.getId(),
                    "Existing billing record returned for timesheet."
            );

            return mapToResponse(existingRecord);
        }

        if (!Boolean.TRUE.equals(timesheet.getBillable())) {
            throw new RuntimeException("Timesheet is not marked as billable.");
        }

        Double billingRate = request.getBillingRate() != null
                ? request.getBillingRate()
                : timesheet.getBillingRate();

        if (billingRate == null || billingRate <= 0) {
            throw new RuntimeException("Billing rate must be greater than zero.");
        }

        Double units = timesheet.getTotalHours() != null ? timesheet.getTotalHours() : 0.0;
        Double amount = roundMoney(units * billingRate);

        LocalDate serviceDate = timesheet.getClockInTime() != null
                ? timesheet.getClockInTime().toLocalDate()
                : LocalDate.now();

        BillingRecord billingRecord = BillingRecord.builder()
                .timesheet(timesheet)
                .client(timesheet.getClient())
                .authorization(timesheet.getAuthorization())
                .serviceDate(serviceDate)
                .units(units)
                .billingRate(billingRate)
                .amount(amount)
                .status(defaultValue(request.getStatus(), "READY_TO_CLAIM"))
                .claimNumber(request.getClaimNumber())
                .paidAmount(request.getPaidAmount() != null ? request.getPaidAmount() : 0.0)
                .paidDate(request.getPaidDate())
                .notes(request.getNotes())
                .build();

        BillingRecord savedRecord = billingRecordRepository.save(billingRecord);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedRecord.getClient().getId(),
                "CREATE_BILLING_RECORD",
                "BILLING_RECORD",
                savedRecord.getId(),
                "Billing record created from timesheet."
        );

        return mapToResponse(savedRecord);
    }

    public BillingRecordResponse updateBillingRecord(Long id, BillingRecordRequest request) {
        BillingRecord record = billingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing record not found."));

        User actor = getActor(request.getActorUserId());

        String previousStatus = record.getStatus();

        if (request.getBillingRate() != null) {
            record.setBillingRate(request.getBillingRate());
            record.setAmount(roundMoney(record.getUnits() * request.getBillingRate()));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            record.setStatus(request.getStatus().toUpperCase());
        }

        record.setClaimNumber(request.getClaimNumber());
        record.setPaidAmount(request.getPaidAmount());
        record.setPaidDate(request.getPaidDate());
        record.setNotes(request.getNotes());

        BillingRecord savedRecord = billingRecordRepository.save(record);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedRecord.getClient().getId(),
                getBillingAuditAction(previousStatus, savedRecord.getStatus()),
                "BILLING_RECORD",
                savedRecord.getId(),
                "Billing record status changed from " + previousStatus + " to " + savedRecord.getStatus() + "."
        );

        return mapToResponse(savedRecord);
    }

    public BillingRecordResponse submitClaim(Long id, BillingRecordRequest request) {
        BillingRecord record = billingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing record not found."));

        User actor = getActor(request.getActorUserId());

        record.setStatus("CLAIM_SUBMITTED");
        record.setClaimNumber(request.getClaimNumber());
        record.setNotes(request.getNotes());

        BillingRecord savedRecord = billingRecordRepository.save(record);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedRecord.getClient().getId(),
                "SUBMIT_CLAIM",
                "BILLING_RECORD",
                savedRecord.getId(),
                "Claim submitted."
        );

        return mapToResponse(savedRecord);
    }

    public BillingRecordResponse markPaid(Long id, BillingRecordRequest request) {
        BillingRecord record = billingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing record not found."));

        User actor = getActor(request.getActorUserId());

        record.setStatus("PAID");
        record.setPaidAmount(request.getPaidAmount() != null ? request.getPaidAmount() : record.getAmount());
        record.setPaidDate(request.getPaidDate() != null ? request.getPaidDate() : LocalDate.now());
        record.setNotes(request.getNotes());

        BillingRecord savedRecord = billingRecordRepository.save(record);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedRecord.getClient().getId(),
                "MARK_BILLING_PAID",
                "BILLING_RECORD",
                savedRecord.getId(),
                "Billing record marked as paid."
        );

        return mapToResponse(savedRecord);
    }

    public BillingRecordResponse denyClaim(Long id, BillingRecordRequest request) {
        BillingRecord record = billingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing record not found."));

        User actor = getActor(request.getActorUserId());

        record.setStatus("DENIED");
        record.setNotes(request.getNotes());

        BillingRecord savedRecord = billingRecordRepository.save(record);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedRecord.getClient().getId(),
                "DENY_CLAIM",
                "BILLING_RECORD",
                savedRecord.getId(),
                "Claim denied."
        );

        return mapToResponse(savedRecord);
    }

    public BillingRecordResponse voidBillingRecord(Long id, BillingRecordRequest request) {
        BillingRecord record = billingRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing record not found."));

        User actor = getActor(request.getActorUserId());

        record.setStatus("VOID");
        record.setNotes(request.getNotes());

        BillingRecord savedRecord = billingRecordRepository.save(record);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedRecord.getClient().getId(),
                "VOID_BILLING_RECORD",
                "BILLING_RECORD",
                savedRecord.getId(),
                "Billing record voided."
        );

        return mapToResponse(savedRecord);
    }

    public List<BillingRecordResponse> getAllBillingRecords() {
        return billingRecordRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BillingRecordResponse> getBillingRecordsByClient(Long clientId) {
        return billingRecordRepository.findByClientIdOrderByServiceDateDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BillingRecordResponse> getBillingRecordsByStatus(String status) {
        return billingRecordRepository.findByStatusOrderByServiceDateDesc(status.toUpperCase())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new RuntimeException("Actor user is required for audit logging.");
        }

        return userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("Actor user not found."));
    }

    private String defaultValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.toUpperCase();
    }

    private String getBillingAuditAction(String previousStatus, String newStatus) {
        if (newStatus == null) {
            return "UPDATE_BILLING_RECORD";
        }

        return switch (newStatus.toUpperCase()) {
            case "READY_TO_CLAIM" -> "APPROVE_BILLING_RECORD";
            case "CLAIM_SUBMITTED" -> "SUBMIT_CLAIM";
            case "PAID" -> "MARK_BILLING_PAID";
            case "DENIED" -> "DENY_CLAIM";
            case "VOID" -> "VOID_BILLING_RECORD";
            default -> "UPDATE_BILLING_RECORD";
        };
    }

    private Double roundMoney(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private BillingRecordResponse mapToResponse(BillingRecord record) {
        return BillingRecordResponse.builder()
                .id(record.getId())
                .timesheetId(record.getTimesheet().getId())
                .clientId(record.getClient().getId())
                .clientName(record.getClient().getFullName())
                .authorizationId(record.getAuthorization() != null ? record.getAuthorization().getId() : null)
                .authorizationNumber(record.getAuthorization() != null ? record.getAuthorization().getAuthorizationNumber() : null)
                .serviceDate(record.getServiceDate())
                .units(record.getUnits())
                .billingRate(record.getBillingRate())
                .amount(record.getAmount())
                .status(record.getStatus())
                .claimNumber(record.getClaimNumber())
                .paidAmount(record.getPaidAmount())
                .paidDate(record.getPaidDate())
                .notes(record.getNotes())
                .build();
    }
}