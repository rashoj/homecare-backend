package com.homecare.service;

import com.homecare.dto.WorkforceReadinessIssue;
import com.homecare.dto.WorkforceReadinessResponse;
import com.homecare.dto.WorkforceRequirementStatus;
import com.homecare.entity.CaregiverComplianceRecord;
import com.homecare.entity.OrganizationComplianceRule;
import com.homecare.entity.User;
import com.homecare.repository.CaregiverComplianceRepository;
import com.homecare.repository.OrganizationComplianceRuleRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkforceReadinessService {

    private final UserRepository userRepository;
    private final OrganizationComplianceRuleRepository ruleRepository;
    private final CaregiverComplianceRepository complianceRepository;

    public WorkforceReadinessService(
            UserRepository userRepository,
            OrganizationComplianceRuleRepository ruleRepository,
            CaregiverComplianceRepository complianceRepository
    ) {
        this.userRepository = userRepository;
        this.ruleRepository = ruleRepository;
        this.complianceRepository = complianceRepository;
    }

    public WorkforceReadinessResponse getReadiness(
            Long caregiverId,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        if (caregiver.getOrganization() == null ||
                !caregiver.getOrganization().getId().equals(actor.getOrganization().getId())) {
            throw new RuntimeException("Caregiver does not belong to this organization.");
        }

        Long organizationId = actor.getOrganization().getId();

        List<OrganizationComplianceRule> rules =
                ruleRepository.findByOrganizationIdAndActiveTrueOrderByRecordTypeAsc(
                        organizationId
                );

        List<CaregiverComplianceRecord> records =
                complianceRepository.findByCaregiverIdOrderByCreatedAtDesc(caregiverId);

        Map<String, CaregiverComplianceRecord> latestRecordByType =
                records.stream()
                        .collect(Collectors.toMap(
                                CaregiverComplianceRecord::getRecordType,
                                record -> record,
                                (existing, replacement) -> existing
                        ));

        List<WorkforceRequirementStatus> requirementStatuses = new ArrayList<>();
        List<String> blockingReasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<WorkforceReadinessIssue> blockingRequirements = new ArrayList<>();
        List<WorkforceReadinessIssue> warningRequirements = new ArrayList<>();

        int totalWeight = 0;
        int earnedWeight = 0;
        int completedRules = 0;
        int missingRules = 0;
        int expiredRules = 0;
        int warningRules = 0;

        LocalDate today = LocalDate.now();

        for (OrganizationComplianceRule rule : rules) {
            int weight = rule.getWeight() != null ? rule.getWeight() : 0;
            totalWeight += weight;

            CaregiverComplianceRecord record =
                    latestRecordByType.get(rule.getRecordType());

            boolean missing = record == null;
            boolean expired = false;
            boolean expiringSoon = false;
            boolean completed = false;
            boolean failed = false;

            String status = missing ? "MISSING" : record.getStatus();

            if (!missing) {
                completed = "COMPLETED".equalsIgnoreCase(record.getStatus());
                failed = "FAILED".equalsIgnoreCase(record.getStatus());

                if (record.getExpirationDate() != null) {
                    expired = record.getExpirationDate().isBefore(today);

                    int warningDays = rule.getWarningDaysBeforeExpiration() != null
                            ? rule.getWarningDaysBeforeExpiration()
                            : 30;

                    expiringSoon =
                            !expired &&
                                    !record.getExpirationDate().isAfter(today.plusDays(warningDays));
                }
            }

            boolean valid = completed && !expired && !failed;

            if (valid) {
                earnedWeight += weight;
                completedRules++;
            }

            if (missing) {
                missingRules++;
            }

            if (expired || failed) {
                expiredRules++;
            }

            if (expiringSoon) {
                warningRules++;
            }

            boolean blocking =
                    Boolean.TRUE.equals(rule.getBlockScheduling()) &&
                            (missing || expired || failed);

            String message = buildMessage(rule, record, missing, expired, expiringSoon, failed);
            String reason = getReason(missing, expired, expiringSoon, failed);

            if (blocking) {
                blockingReasons.add(message);

                blockingRequirements.add(
                        WorkforceReadinessIssue.builder()
                                .recordType(rule.getRecordType())
                                .displayName(rule.getDisplayName())
                                .reason(reason)
                                .required(rule.getRequired())
                                .blockScheduling(rule.getBlockScheduling())
                                .expirationDate(record != null ? record.getExpirationDate() : null)
                                .message(message)
                                .build()
                );
            }

            if (expiringSoon) {
                warnings.add(message);

                warningRequirements.add(
                        WorkforceReadinessIssue.builder()
                                .recordType(rule.getRecordType())
                                .displayName(rule.getDisplayName())
                                .reason(reason)
                                .required(rule.getRequired())
                                .blockScheduling(rule.getBlockScheduling())
                                .expirationDate(record != null ? record.getExpirationDate() : null)
                                .message(message)
                                .build()
                );
            }

            requirementStatuses.add(
                    WorkforceRequirementStatus.builder()
                            .recordType(rule.getRecordType())
                            .displayName(rule.getDisplayName())
                            .required(rule.getRequired())
                            .blockScheduling(rule.getBlockScheduling())
                            .weight(weight)
                            .status(status)
                            .completedDate(record != null ? record.getCompletedDate() : null)
                            .expirationDate(record != null ? record.getExpirationDate() : null)
                            .missing(missing)
                            .expired(expired)
                            .expiringSoon(expiringSoon)
                            .blocking(blocking)
                            .message(message)
                            .build()
            );
        }

        int score = totalWeight == 0
                ? 100
                : Math.round((earnedWeight * 100.0f) / totalWeight);

        boolean schedulingBlocked = !blockingReasons.isEmpty();

        return WorkforceReadinessResponse.builder()
                .caregiverId(caregiver.getId())
                .caregiverName(caregiver.getFullName())
                .organizationId(organizationId)
                .readinessScore(score)
                .readyToWork(!schedulingBlocked)
                .schedulingBlocked(schedulingBlocked)
                .totalRules(rules.size())
                .completedRules(completedRules)
                .missingRules(missingRules)
                .expiredRules(expiredRules)
                .warningRules(warningRules)
                .blockingReasons(blockingReasons)
                .warnings(warnings)
                .requirements(requirementStatuses)
                .blockingRequirements(blockingRequirements)
                .warningRequirements(warningRequirements)
                .build();
    }

    private String buildMessage(
            OrganizationComplianceRule rule,
            CaregiverComplianceRecord record,
            boolean missing,
            boolean expired,
            boolean expiringSoon,
            boolean failed
    ) {
        String name = rule.getDisplayName() != null
                ? rule.getDisplayName()
                : rule.getRecordType();

        if (missing) {
            return name + " is missing.";
        }

        if (failed) {
            return name + " failed.";
        }

        if (expired) {
            return name + " is expired.";
        }

        if (expiringSoon && record.getExpirationDate() != null) {
            return name + " expires on " + record.getExpirationDate() + ".";
        }

        return name + " is compliant.";
    }
    private String getReason(
            boolean missing,
            boolean expired,
            boolean expiringSoon,
            boolean failed
    ) {
        if (missing) {
            return "MISSING";
        }

        if (failed) {
            return "FAILED";
        }

        if (expired) {
            return "EXPIRED";
        }

        if (expiringSoon) {
            return "EXPIRING_SOON";
        }

        return "COMPLIANT";
    }

    private User getActor(String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return actor;
    }
}