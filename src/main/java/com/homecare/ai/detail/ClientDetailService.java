package com.homecare.ai.detail;

import com.homecare.ai.projection.ClientRiskProjection;
import com.homecare.ai.service.ClientRiskAggregationService;
import com.homecare.dto.AiCopilotDetailDTO;
import com.homecare.entity.Client;
import com.homecare.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientDetailService {

    private final ClientRepository clientRepository;
    private final ClientRiskAggregationService clientRiskAggregationService;

    public ClientDetailService(
            ClientRepository clientRepository,
            ClientRiskAggregationService clientRiskAggregationService
    ) {
        this.clientRepository = clientRepository;
        this.clientRiskAggregationService = clientRiskAggregationService;
    }

    public List<AiCopilotDetailDTO> searchClients(
            Long organizationId,
            String searchTerm
    ) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        Map<Long, ClientRiskProjection> riskMap =
                clientRiskAggregationService.getRiskMap(organizationId);

        return clientRepository
                .findTop10ByOrganizationIdAndFullNameContainingIgnoreCaseOrderByFullNameAsc(
                        organizationId,
                        searchTerm.trim()
                )
                .stream()
                .map(client ->
                        toDetailDTO(
                                client,
                                riskMap.get(client.getId())
                        )
                )
                .toList();
    }

    public List<AiCopilotDetailDTO> getActiveClients(
            Long organizationId
    ) {
        Map<Long, ClientRiskProjection> riskMap =
                clientRiskAggregationService.getRiskMap(organizationId);

        return clientRepository
                .findTop10ByOrganizationIdAndActiveTrueOrderByFullNameAsc(
                        organizationId
                )
                .stream()
                .map(client ->
                        toDetailDTO(
                                client,
                                riskMap.get(client.getId())
                        )
                )
                .toList();
    }

    public List<AiCopilotDetailDTO> getClientsWithOpenEVVIssues(
            Long organizationId
    ) {
        Map<Long, ClientRiskProjection> riskMap =
                clientRiskAggregationService.getRiskMap(organizationId);

        return clientRepository
                .findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .filter(client ->
                        getOpenEVVIssues(
                                riskMap.get(client.getId())
                        ) > 0
                )
                .map(client ->
                        toDetailDTO(
                                client,
                                riskMap.get(client.getId())
                        )
                )
                .limit(10)
                .toList();
    }

    public List<AiCopilotDetailDTO> getClientsWithActiveIncidents(
            Long organizationId
    ) {
        Map<Long, ClientRiskProjection> riskMap =
                clientRiskAggregationService.getRiskMap(organizationId);

        return clientRepository
                .findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .filter(client ->
                        getActiveIncidents(
                                riskMap.get(client.getId())
                        ) > 0
                )
                .map(client ->
                        toDetailDTO(
                                client,
                                riskMap.get(client.getId())
                        )
                )
                .limit(10)
                .toList();
    }

    public Optional<Client> findSingleClient(
            Long organizationId,
            String searchTerm
    ) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return Optional.empty();
        }

        List<Client> matches =
                clientRepository
                        .findTop10ByOrganizationIdAndFullNameContainingIgnoreCaseOrderByFullNameAsc(
                                organizationId,
                                searchTerm.trim()
                        );

        if (matches.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(matches.get(0));
    }

    public long getActiveClientCount(Long organizationId) {
        return clientRepository.countByOrganizationIdAndActiveTrue(
                organizationId
        );
    }

    private AiCopilotDetailDTO toDetailDTO(
            Client client,
            ClientRiskProjection risk
    ) {
        long openEVVIssues = getOpenEVVIssues(risk);
        long highSeverityEVVIssues = getHighSeverityEVVIssues(risk);
        long activeIncidents = getActiveIncidents(risk);
        long highRiskIncidents = getHighRiskIncidents(risk);

        String severity = determineClientSeverity(
                highSeverityEVVIssues,
                activeIncidents,
                highRiskIncidents
        );

        boolean active =
                Boolean.TRUE.equals(client.getActive());

        return new AiCopilotDetailDTO(
                valueOrDefault(
                        client.getFullName(),
                        "Unknown Client"
                ),
                active
                        ? "ACTIVE CLIENT"
                        : "INACTIVE CLIENT",
                buildClientDescription(
                        openEVVIssues,
                        activeIncidents
                ),
                "CLIENT",
                severity,
                active
                        ? "ACTIVE"
                        : "INACTIVE",
                "/clients/" + client.getId(),
                Map.of(
                        "Age",
                        calculateAge(client.getDateOfBirth()),

                        "Mobility",
                        valueOrDash(client.getMobilityStatus()),

                        "Open EVV Issues",
                        String.valueOf(openEVVIssues),

                        "High Severity EVV",
                        String.valueOf(highSeverityEVVIssues),

                        "Active Incidents",
                        String.valueOf(activeIncidents),

                        "High Risk Incidents",
                        String.valueOf(highRiskIncidents)
                )
        );
    }

    private long getOpenEVVIssues(ClientRiskProjection risk) {
        return risk != null && risk.getOpenEvvIssues() != null
                ? risk.getOpenEvvIssues()
                : 0L;
    }

    private long getHighSeverityEVVIssues(
            ClientRiskProjection risk
    ) {
        return risk != null
                && risk.getHighSeverityEvvIssues() != null
                ? risk.getHighSeverityEvvIssues()
                : 0L;
    }

    private long getActiveIncidents(ClientRiskProjection risk) {
        return risk != null && risk.getActiveIncidents() != null
                ? risk.getActiveIncidents()
                : 0L;
    }

    private long getHighRiskIncidents(
            ClientRiskProjection risk
    ) {
        return risk != null && risk.getHighRiskIncidents() != null
                ? risk.getHighRiskIncidents()
                : 0L;
    }

    private String determineClientSeverity(
            long highSeverityEVVIssues,
            long activeIncidents,
            long highRiskIncidents
    ) {
        if (highRiskIncidents > 0) {
            return "CRITICAL";
        }

        if (highSeverityEVVIssues > 0) {
            return "HIGH";
        }

        if (activeIncidents > 0) {
            return "MEDIUM";
        }

        return "NORMAL";
    }

    private String buildClientDescription(
            long openEVVIssues,
            long activeIncidents
    ) {
        return """
                Current operational profile: %d open EVV issues and %d active incidents.
                """.formatted(
                openEVVIssues,
                activeIncidents
        ).trim();
    }

    private String calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "—";
        }

        return String.valueOf(
                Period.between(
                        dateOfBirth,
                        LocalDate.now()
                ).getYears()
        );
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank()
                ? "—"
                : value;
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