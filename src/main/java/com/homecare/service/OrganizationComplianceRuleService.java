package com.homecare.service;

import com.homecare.dto.OrganizationComplianceRuleRequest;
import com.homecare.dto.OrganizationComplianceRuleResponse;
import com.homecare.entity.OrganizationComplianceRule;
import com.homecare.entity.User;
import com.homecare.repository.OrganizationComplianceRuleRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationComplianceRuleService {

    private final OrganizationComplianceRuleRepository ruleRepository;
    private final UserRepository userRepository;

    public OrganizationComplianceRuleService(
            OrganizationComplianceRuleRepository ruleRepository,
            UserRepository userRepository
    ) {
        this.ruleRepository = ruleRepository;
        this.userRepository = userRepository;
    }

    public OrganizationComplianceRuleResponse createOrUpdate(
            OrganizationComplianceRuleRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Long organizationId = actor.getOrganization().getId();

        OrganizationComplianceRule rule = ruleRepository
                .findByOrganizationIdAndRecordType(organizationId, request.getRecordType())
                .orElseGet(() -> OrganizationComplianceRule.builder()
                        .organization(actor.getOrganization())
                        .recordType(request.getRecordType())
                        .build());

        rule.setDisplayName(request.getDisplayName());
        rule.setRequired(request.getRequired());
        rule.setWeight(request.getWeight());
        rule.setBlockScheduling(request.getBlockScheduling());
        rule.setActive(request.getActive());
        rule.setWarningDaysBeforeExpiration(request.getWarningDaysBeforeExpiration());

        return mapToResponse(ruleRepository.save(rule));
    }

    public List<OrganizationComplianceRuleResponse> getRules(String actorEmail) {
        User actor = getActor(actorEmail);

        return ruleRepository
                .findByOrganizationIdOrderByRecordTypeAsc(actor.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrganizationComplianceRuleResponse setActive(
            Long id,
            Boolean active,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);

        OrganizationComplianceRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance rule not found."));

        if (!rule.getOrganization().getId().equals(actor.getOrganization().getId())) {
            throw new RuntimeException("Compliance rule does not belong to this organization.");
        }

        rule.setActive(active);

        return mapToResponse(ruleRepository.save(rule));
    }

    private User getActor(String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null || actor.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return actor;
    }

    private OrganizationComplianceRuleResponse mapToResponse(
            OrganizationComplianceRule rule
    ) {
        return OrganizationComplianceRuleResponse.builder()
                .id(rule.getId())
                .organizationId(rule.getOrganization().getId())
                .recordType(rule.getRecordType())
                .displayName(rule.getDisplayName())
                .required(rule.getRequired())
                .weight(rule.getWeight())
                .blockScheduling(rule.getBlockScheduling())
                .active(rule.getActive())
                .warningDaysBeforeExpiration(rule.getWarningDaysBeforeExpiration())
                .build();
    }
}