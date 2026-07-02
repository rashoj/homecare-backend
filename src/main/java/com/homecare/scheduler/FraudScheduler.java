package com.homecare.scheduler;

import com.homecare.service.FraudDetectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FraudScheduler {

    private final FraudDetectionService fraudDetectionService;

    public FraudScheduler(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void runFraudDetection() {
        fraudDetectionService.detectMissingClockOuts();
    }
}