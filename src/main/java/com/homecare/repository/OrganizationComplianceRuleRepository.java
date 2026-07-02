package com.homecare.repository;

import com.homecare.entity.OrganizationComplianceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationComplianceRuleRepository
        extends JpaRepository<OrganizationComplianceRule, Long> {

    List<OrganizationComplianceRule> findByOrganizationIdOrderByRecordTypeAsc(
            Long organizationId
    );

    List<OrganizationComplianceRule> findByOrganizationIdAndActiveTrueOrderByRecordTypeAsc(
            Long organizationId
    );

    Optional<OrganizationComplianceRule> findByOrganizationIdAndRecordType(
            Long organizationId,
            String recordType
    );
}