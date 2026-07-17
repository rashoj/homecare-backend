package com.homecare.ai.service;

import com.homecare.ai.projection.ClientRiskProjection;
import com.homecare.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClientRiskAggregationService {

    private final ClientRepository clientRepository;

    public ClientRiskAggregationService(
            ClientRepository clientRepository
    ) {
        this.clientRepository = clientRepository;
    }

    public Map<Long, ClientRiskProjection> getRiskMap(
            Long organizationId
    ) {
        return clientRepository
                .findClientRiskSummary(organizationId)
                .stream()
                .collect(
                        Collectors.toMap(
                                ClientRiskProjection::getClientId,
                                Function.identity()
                        )
                );
    }
}