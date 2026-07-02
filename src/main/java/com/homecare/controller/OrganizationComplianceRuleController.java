package com.homecare.controller;

import com.homecare.dto.OrganizationComplianceRuleRequest;
import com.homecare.dto.OrganizationComplianceRuleResponse;
import com.homecare.service.OrganizationComplianceRuleService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization-compliance-rules")
@CrossOrigin("*")
public class OrganizationComplianceRuleController {

    private final OrganizationComplianceRuleService service;

    public OrganizationComplianceRuleController(
            OrganizationComplianceRuleService service
    ) {
        this.service = service;
    }

    @PostMapping
    public OrganizationComplianceRuleResponse createOrUpdate(
            @RequestBody OrganizationComplianceRuleRequest request,
            Authentication authentication
    ) {
        return service.createOrUpdate(request, authentication.getName());
    }

    @GetMapping
    public List<OrganizationComplianceRuleResponse> getRules(
            Authentication authentication
    ) {
        return service.getRules(authentication.getName());
    }

    @PutMapping("/{id}/active")
    public OrganizationComplianceRuleResponse setActive(
            @PathVariable Long id,
            @RequestParam Boolean active,
            Authentication authentication
    ) {
        return service.setActive(id, active, authentication.getName());
    }
}