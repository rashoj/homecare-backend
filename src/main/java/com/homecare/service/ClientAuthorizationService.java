package com.homecare.service;

import com.homecare.dto.ClientAuthorizationRequest;
import com.homecare.dto.ClientAuthorizationResponse;
import com.homecare.entity.Client;
import com.homecare.entity.ClientAuthorization;
import com.homecare.repository.ClientAuthorizationRepository;
import com.homecare.repository.ClientRepository;
import org.springframework.stereotype.Service;
import com.homecare.repository.TimesheetRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientAuthorizationService {

    private final ClientAuthorizationRepository authorizationRepository;
    private final ClientRepository clientRepository;
    private final TimesheetRepository timesheetRepository;

    public ClientAuthorizationService(
            ClientAuthorizationRepository authorizationRepository,
            ClientRepository clientRepository,
            TimesheetRepository timesheetRepository
    ) {
        this.authorizationRepository = authorizationRepository;
        this.clientRepository = clientRepository;
        this.timesheetRepository = timesheetRepository;
    }

    public ClientAuthorizationResponse createAuthorization(
            ClientAuthorizationRequest request
    ) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

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

        return mapToResponse(authorizationRepository.save(authorization));
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

    public ClientAuthorizationResponse updateAuthorization(
            Long id,
            ClientAuthorizationRequest request
    ) {
        ClientAuthorization authorization = authorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authorization not found"));

        authorization.setAuthorizationNumber(request.getAuthorizationNumber());
        authorization.setServiceCode(request.getServiceCode());
        authorization.setStartDate(request.getStartDate());
        authorization.setEndDate(request.getEndDate());
        authorization.setApprovedWeeklyHours(request.getApprovedWeeklyHours());
        authorization.setApprovedTotalHours(request.getApprovedTotalHours());
        authorization.setNotes(request.getNotes());

        return mapToResponse(authorizationRepository.save(authorization));
    }

    public void closeAuthorization(Long id) {
        ClientAuthorization authorization = authorizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authorization not found"));

        authorization.setStatus("CLOSED");
        authorizationRepository.save(authorization);
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
}