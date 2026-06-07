package com.homecare.controller;

import com.homecare.dto.OrganizationCreateRequest;
import com.homecare.dto.OrganizationResponse;
import com.homecare.dto.OrganizationUpdateRequest;
import com.homecare.service.OrganizationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.homecare.dto.CreateAgencyAdminRequest;
import com.homecare.dto.OrganizationUserResponse;

import java.util.List;

@RestController
@RequestMapping("/api/platform/organizations")
@CrossOrigin("*")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(
            OrganizationService organizationService
    ) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public OrganizationResponse createOrganization(
            @RequestBody OrganizationCreateRequest request,
            Authentication authentication
    ) {
        return organizationService.createOrganization(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<OrganizationResponse> getOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @GetMapping("/{id}")
    public OrganizationResponse getOrganization(
            @PathVariable Long id
    ) {
        return organizationService.getOrganizationById(id);
    }

    @PutMapping("/{id}")
    public OrganizationResponse updateOrganization(
            @PathVariable Long id,
            @RequestBody OrganizationUpdateRequest request,
            Authentication authentication
    ) {
        return organizationService.updateOrganization(
                id,
                request,
                authentication.getName()
        );
    }
    @PostMapping("/{id}/agency-admin")
    public OrganizationUserResponse createAgencyAdmin(
            @PathVariable Long id,
            @RequestBody CreateAgencyAdminRequest request,
            Authentication authentication
    ) {
        return organizationService.createAgencyAdmin(
                id,
                request,
                authentication.getName()
        );
    }

    @GetMapping("/{id}/users")
    public List<OrganizationUserResponse> getOrganizationUsers(
            @PathVariable Long id
    ) {
        return organizationService.getOrganizationUsers(id);
    }
}