package com.homecare.controller;

import com.homecare.dto.BehaviorEventRequest;
import com.homecare.dto.BehaviorEventResponse;
import com.homecare.dto.BehaviorOptionResponse;
import com.homecare.service.BehaviorEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/behavior-events")
@CrossOrigin("*")
public class BehaviorEventController {

    private final BehaviorEventService behaviorEventService;

    public BehaviorEventController(BehaviorEventService behaviorEventService) {
        this.behaviorEventService = behaviorEventService;
    }

    @PostMapping
    public BehaviorEventResponse createEvent(@RequestBody BehaviorEventRequest request) {
        return behaviorEventService.createEvent(request);
    }

    @GetMapping("/client/{clientId}")
    public List<BehaviorEventResponse> getByClient(@PathVariable Long clientId) {
        return behaviorEventService.getByClient(clientId);
    }

    @GetMapping("/service-documentation/{documentationId}")
    public List<BehaviorEventResponse> getByServiceDocumentation(
            @PathVariable Long documentationId
    ) {
        return behaviorEventService.getByServiceDocumentation(documentationId);
    }

    @GetMapping("/options/behavior-types")
    public List<BehaviorOptionResponse> getBehaviorTypes() {
        return behaviorEventService.getBehaviorTypes();
    }

    @GetMapping("/options/triggers")
    public List<BehaviorOptionResponse> getTriggers() {
        return behaviorEventService.getTriggers();
    }

    @GetMapping("/options/severities")
    public List<BehaviorOptionResponse> getSeverities() {
        return behaviorEventService.getSeverities();
    }

    @GetMapping("/options/outcomes")
    public List<BehaviorOptionResponse> getOutcomes() {
        return behaviorEventService.getOutcomes();
    }
}