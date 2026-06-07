package com.homecare.controller;

import com.homecare.dto.DemoRequestCreateRequest;
import com.homecare.dto.DemoRequestResponse;
import com.homecare.service.DemoRequestService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class DemoRequestController {

    private final DemoRequestService demoRequestService;

    public DemoRequestController(DemoRequestService demoRequestService) {
        this.demoRequestService = demoRequestService;
    }

    @PostMapping("/public/demo-requests")
    public DemoRequestResponse createDemoRequest(
            @RequestBody DemoRequestCreateRequest request
    ) {
        return demoRequestService.createDemoRequest(request);
    }

    @GetMapping("/platform/demo-requests")
    public List<DemoRequestResponse> getDemoRequests() {
        return demoRequestService.getAllDemoRequests();
    }

    @PutMapping("/platform/demo-requests/{id}/status")
    public DemoRequestResponse updateDemoRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication
    ) {
        return demoRequestService.updateStatus(
                id,
                status,
                authentication.getName()
        );
    }
}