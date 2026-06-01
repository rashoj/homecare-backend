package com.homecare.service;

import com.homecare.dto.ClientAuthorizationRequest;
import com.homecare.dto.ClientAuthorizationResponse;
import com.homecare.entity.Client;
import com.homecare.entity.ClientAuthorization;
import com.homecare.entity.User;
import com.homecare.repository.ClientAuthorizationRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.TimesheetRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientAuthorizationService {

    private final ClientAuthorizationRepository authorizationRepository;
    private final ClientRepository clientRepository;
    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ClientAuthorizationService(
            ClientAuthorizationRepository authorizationRepository,
            ClientRepository clientRepository,
            TimesheetRepository timesheetRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.authorizationRepository = authorizationRepository;
        this.clientRepository = clientRepository;
        this.timesheetRepository = timesheetRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public ClientAuthorizationResponse createAuthorization(
            ClientAuthorizationRequest request
    ) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User actor = getActor(request.getActorUserId());

        ClientAuthorization authorization = ClientAuthorization.builder()
                .client(client)
                .authorizationNumber(request.getAuthorizationNumber())
                .serviceCode(request.getServiceCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .approvedWeeklyHours(request.getApprovedWeeklyHours())
                .approvedTotalHours(request.getApprovedTotalHours())
                .usedHours(0.0)
                .status("ACTIVE")
                .notes(request.getNotes())
                .build();

        ClientAuthorization savedAuthorization =
                authorizationRepository.save(authorization);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                client.getId(),
                "CREATE_AUTHORIZATION",
                "CLIENT_AUTHORIZATION",
                savedAuthorization.getId(),
                "Client authorization created."
        );

        return mapToResponse(savedAuthorization);
    }

    public ClientAuthorizationResponse updateAuthorization(
            Long id,
            ClientAuthorizationRequest request
    ) {
        ClientAuthorization authorization = authorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authorization not found"));

        User actor = getActor(request.getActorUserId());

        authorization.setAuthorizationNumber(request.getAuthorizationNumber());
        authorization.setServiceCode(request.getServiceCode());
        authorization.setStartDate(request.getStartDate());
        authorization.setEndDate(request.getEndDate());
        authorization.setApprovedWeeklyHours(request.getApprovedWeeklyHours());
        authorization.setApprovedTotalHours(request.getApprovedTotalHours());
        authorization.setNotes(request.getNotes());

        ClientAuthorization savedAuthorization =
                authorizationRepository.save(authorization);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedAuthorization.getClient().getId(),
                "UPDATE_AUTHORIZATION",
                "CLIENT_AUTHORIZATION",
                savedAuthorization.getId(),
                "Client authorization updated."
        );

        return mapToResponse(savedAuthorization);
    }

    public void closeAuthorization(Long id, Long actorUserId) {
        ClientAuthorization authorization = authorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authorization not found"));

        User actor = getActor(actorUserId);

        authorization.setStatus("CLOSED");

        ClientAuthorization savedAuthorization =
                authorizationRepository.save(authorization);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedAuthorization.getClient().getId(),
                "CLOSE_AUTHORIZATION",
                "CLIENT_AUTHORIZATION",
                savedAuthorization.getId(),
                "Client authorization closed."
        );
    }

    public List<ClientAuthorizationResponse> getAuthorizationsByClient(Long clientId) {
        return authorizationRepository.findByClientIdOrderByEndDateDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ClientAuthorizationResponse> getAllAuthorizations() {
        return authorizationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ClientAuthorizationResponse> getExpiringSoonAuthorizations() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);

        return authorizationRepository
                .findByEndDateBetweenAndStatus(today, thirtyDaysFromNow, "ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ClientAuthorizationResponse> getExpiredAuthorizations() {
        return authorizationRepository
                .findByEndDateBeforeAndStatus(LocalDate.now(), "ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ClientAuthorizationResponse mapToResponse(
            ClientAuthorization authorization
    ) {
        Double approvedTotal = authorization.getApprovedTotalHours() != null
                ? authorization.getApprovedTotalHours()
                : 0.0;

        Double used = calculateUsedHours(authorization.getId());

        Double remaining = approvedTotal - used;

        String alertStatus = calculateAlertStatus(authorization, remaining);

        return ClientAuthorizationResponse.builder()
                .id(authorization.getId())
                .clientId(authorization.getClient().getId())
                .clientName(authorization.getClient().getFullName())
                .authorizationNumber(authorization.getAuthorizationNumber())
                .serviceCode(authorization.getServiceCode())
                .startDate(authorization.getStartDate())
                .endDate(authorization.getEndDate())
                .approvedWeeklyHours(authorization.getApprovedWeeklyHours())
                .approvedTotalHours(authorization.getApprovedTotalHours())
                .usedHours(used)
                .remainingHours(remaining)
                .status(authorization.getStatus())
                .alertStatus(alertStatus)
                .notes(authorization.getNotes())
                .build();
    }

    private String calculateAlertStatus(
            ClientAuthorization authorization,
            Double remainingHours
    ) {
        if (remainingHours != null && remainingHours < 0) {
            return "OVER_USED";
        }

        if (authorization.getEndDate() != null
                && authorization.getEndDate().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }

        if (authorization.getEndDate() != null
                && !authorization.getEndDate().isAfter(LocalDate.now().plusDays(30))) {
            return "EXPIRING_SOON";
        }

        return "OK";
    }

    private Double calculateUsedHours(Long authorizationId) {
        return timesheetRepository.findByAuthorizationId(authorizationId)
                .stream()
                .filter(timesheet -> timesheet.getTotalHours() != null)
                .mapToDouble(timesheet -> timesheet.getTotalHours())
                .sum();
    }

    private User getActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new RuntimeException("Actor user is required for audit logging.");
        }

        return userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("Actor user not found."));
    }
}