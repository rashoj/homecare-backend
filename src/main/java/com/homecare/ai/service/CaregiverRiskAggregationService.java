package com.homecare.ai.service;

import com.homecare.ai.projection.CaregiverRiskProjection;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CaregiverRiskAggregationService {

    private final UserRepository userRepository;

    public CaregiverRiskAggregationService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public Map<Long, CaregiverRiskProjection> getRiskMap(
            Long organizationId
    ) {
        return userRepository
                .findCaregiverRiskSummary(organizationId)
                .stream()
                .collect(
                        Collectors.toMap(
                                CaregiverRiskProjection::getCaregiverId,
                                Function.identity()
                        )
                );
    }
}