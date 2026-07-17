package com.homecare.ai.detail;

import com.homecare.ai.projection.CaregiverRiskProjection;
import com.homecare.ai.service.CaregiverRiskAggregationService;
import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CaregiverDetailService {

    private final UserRepository userRepository;
    private final CaregiverRiskAggregationService caregiverRiskAggregationService;

    public CaregiverDetailService(
            UserRepository userRepository,
            CaregiverRiskAggregationService caregiverRiskAggregationService
    ) {
        this.userRepository = userRepository;
        this.caregiverRiskAggregationService =
                caregiverRiskAggregationService;
    }

    public List<AiCopilotDetailDTO> searchCaregivers(
            Long organizationId,
            String searchTerm
    ) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        Map<Long, CaregiverRiskProjection> riskMap =
                caregiverRiskAggregationService.getRiskMap(
                        organizationId
                );

        return userRepository
                .findTop10ByOrganizationIdAndRoleAndFullNameContainingIgnoreCaseOrderByFullNameAsc(
                        organizationId,
                        Role.CAREGIVER,
                        searchTerm.trim()
                )
                .stream()
                .map(caregiver ->
                        toDetailDTO(
                                caregiver,
                                riskMap.get(caregiver.getId())
                        )
                )
                .toList();
    }

    public List<AiCopilotDetailDTO> getActiveCaregivers(
            Long organizationId
    ) {
        Map<Long, CaregiverRiskProjection> riskMap =
                caregiverRiskAggregationService.getRiskMap(
                        organizationId
                );

        return userRepository
                .findTop10ByOrganizationIdAndRoleAndActiveTrueOrderByFullNameAsc(
                        organizationId,
                        Role.CAREGIVER
                )
                .stream()
                .map(caregiver ->
                        toDetailDTO(
                                caregiver,
                                riskMap.get(caregiver.getId())
                        )
                )
                .toList();
    }

    public List<AiCopilotDetailDTO> getCaregiversWithOpenEVVIssues(
            Long organizationId
    ) {
        Map<Long, CaregiverRiskProjection> riskMap =
                caregiverRiskAggregationService.getRiskMap(
                        organizationId
                );

        return userRepository
                .findByOrganizationIdAndRoleAndActiveTrue(
                        organizationId,
                        Role.CAREGIVER
                )
                .stream()
                .filter(caregiver ->
                        getOpenEVVIssues(
                                riskMap.get(caregiver.getId())
                        ) > 0
                )
                .map(caregiver ->
                        toDetailDTO(
                                caregiver,
                                riskMap.get(caregiver.getId())
                        )
                )
                .limit(10)
                .toList();
    }

    public List<AiCopilotDetailDTO> getCaregiversWithHighSeverityEVV(
            Long organizationId
    ) {
        Map<Long, CaregiverRiskProjection> riskMap =
                caregiverRiskAggregationService.getRiskMap(
                        organizationId
                );

        return userRepository
                .findByOrganizationIdAndRoleAndActiveTrue(
                        organizationId,
                        Role.CAREGIVER
                )
                .stream()
                .filter(caregiver ->
                        getHighSeverityEVVIssues(
                                riskMap.get(caregiver.getId())
                        ) > 0
                )
                .map(caregiver ->
                        toDetailDTO(
                                caregiver,
                                riskMap.get(caregiver.getId())
                        )
                )
                .limit(10)
                .toList();
    }

    public long getActiveCaregiverCount(Long organizationId) {
        return userRepository
                .countByOrganizationIdAndRoleAndActiveTrue(
                        organizationId,
                        Role.CAREGIVER
                );
    }

    private AiCopilotDetailDTO toDetailDTO(
            User caregiver,
            CaregiverRiskProjection risk
    ) {
        long openEVVIssues =
                getOpenEVVIssues(risk);

        long highSeverityEVVIssues =
                getHighSeverityEVVIssues(risk);

        boolean active =
                Boolean.TRUE.equals(caregiver.getActive());

        return new AiCopilotDetailDTO(
                valueOrDefault(
                        caregiver.getFullName(),
                        "Unknown Caregiver"
                ),
                active
                        ? "ACTIVE CAREGIVER"
                        : "INACTIVE CAREGIVER",
                buildDescription(
                        openEVVIssues,
                        highSeverityEVVIssues
                ),
                "CAREGIVER",
                determineSeverity(
                        highSeverityEVVIssues
                ),
                active
                        ? "ACTIVE"
                        : "INACTIVE",
                "/caregivers/" + caregiver.getId(),
                Map.of(
                        "Role",
                        caregiver.getRole() != null
                                ? caregiver.getRole().name()
                                : "—",

                        "Open EVV Issues",
                        String.valueOf(openEVVIssues),

                        "High Severity EVV",
                        String.valueOf(highSeverityEVVIssues)
                )
        );
    }

    private long getOpenEVVIssues(
            CaregiverRiskProjection risk
    ) {
        return risk != null
                && risk.getOpenEvvIssues() != null
                ? risk.getOpenEvvIssues()
                : 0L;
    }

    private long getHighSeverityEVVIssues(
            CaregiverRiskProjection risk
    ) {
        return risk != null
                && risk.getHighSeverityEvvIssues() != null
                ? risk.getHighSeverityEvvIssues()
                : 0L;
    }

    private String determineSeverity(
            long highSeverityEVVIssues
    ) {
        return highSeverityEVVIssues > 0
                ? "HIGH"
                : "NORMAL";
    }

    private String buildDescription(
            long openEVVIssues,
            long highSeverityEVVIssues
    ) {
        return """
                Current operational profile: %d open EVV issues and %d high-severity EVV issues.
                """.formatted(
                openEVVIssues,
                highSeverityEVVIssues
        ).trim();
    }

    private String valueOrDefault(
            String value,
            String defaultValue
    ) {
        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }
}