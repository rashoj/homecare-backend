package com.homecare.service;

import com.homecare.dto.OrganizationCreateRequest;
import com.homecare.dto.OrganizationResponse;
import com.homecare.dto.OrganizationUpdateRequest;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.OrganizationRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.homecare.dto.CreateAgencyAdminRequest;
import com.homecare.dto.OrganizationUserResponse;
import com.homecare.entity.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            AuditLogService auditLogService,PasswordEncoder passwordEncoder
    ) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    public OrganizationResponse createOrganization(
            OrganizationCreateRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);

        Organization organization = Organization.builder()
                .name(request.getName())
                .legalName(request.getLegalName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .medicaidProviderNumber(request.getMedicaidProviderNumber())
                .npiNumber(request.getNpiNumber())
                .status("LEAD")
                .build();

        Organization saved = organizationRepository.save(organization);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                null,
                "ORGANIZATION_CREATED",
                "ORGANIZATION",
                saved.getId(),
                "Organization created: " + saved.getName()
        );

        return mapToResponse(saved);
    }

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrganizationResponse getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        return mapToResponse(organization);
    }

    public OrganizationResponse updateOrganization(
            Long id,
            OrganizationUpdateRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        String oldStatus = organization.getStatus();

        organization.setName(request.getName());
        organization.setLegalName(request.getLegalName());
        organization.setEmail(request.getEmail());
        organization.setPhone(request.getPhone());
        organization.setAddressLine1(request.getAddressLine1());
        organization.setAddressLine2(request.getAddressLine2());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        organization.setZipCode(request.getZipCode());
        organization.setMedicaidProviderNumber(request.getMedicaidProviderNumber());
        organization.setNpiNumber(request.getNpiNumber());

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            organization.setStatus(request.getStatus());
        }

        Organization saved = organizationRepository.save(organization);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                null,
                "ORGANIZATION_UPDATED",
                "ORGANIZATION",
                saved.getId(),
                "Organization updated: "
                        + saved.getName()
                        + ". Status changed from "
                        + oldStatus
                        + " to "
                        + saved.getStatus()
        );

        return mapToResponse(saved);
    }

    private User getActor(String actorEmail) {
        return userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("Actor user not found"));
    }

    private OrganizationResponse mapToResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .legalName(organization.getLegalName())
                .email(organization.getEmail())
                .phone(organization.getPhone())
                .addressLine1(organization.getAddressLine1())
                .addressLine2(organization.getAddressLine2())
                .city(organization.getCity())
                .state(organization.getState())
                .zipCode(organization.getZipCode())
                .medicaidProviderNumber(organization.getMedicaidProviderNumber())
                .npiNumber(organization.getNpiNumber())
                .status(organization.getStatus())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }
    public OrganizationUserResponse createAgencyAdmin(
            Long organizationId,
            CreateAgencyAdminRequest request,
            String actorEmail
    ) {
        User actor = getActor(actorEmail);

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("A user with this email already exists");
        }

        User agencyAdmin = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.AGENCY_ADMIN)
                .organization(organization)
                .active(true)
                .build();

        User saved = userRepository.save(agencyAdmin);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                null,
                "AGENCY_ADMIN_CREATED",
                "ORGANIZATION",
                organization.getId(),
                "Agency admin created for organization "
                        + organization.getName()
                        + ": "
                        + saved.getEmail()
        );

        return mapUserToResponse(saved);
    }

    public List<OrganizationUserResponse> getOrganizationUsers(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        return userRepository.findByOrganizationIdOrderByCreatedAtDesc(organization.getId())
                .stream()
                .map(this::mapUserToResponse)
                .toList();
    }

    private OrganizationUserResponse mapUserToResponse(User user) {
        return OrganizationUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phoneNumber(user.getPhoneNumber())
                .active(user.getActive())
                .organizationId(
                        user.getOrganization() != null
                                ? user.getOrganization().getId()
                                : null
                )
                .organizationName(
                        user.getOrganization() != null
                                ? user.getOrganization().getName()
                                : null
                )
                .createdAt(user.getCreatedAt())
                .build();
    }
}