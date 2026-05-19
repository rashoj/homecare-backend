package com.homecare.controller;

import com.homecare.dto.ClientAuthorizationRequest;
import com.homecare.dto.ClientAuthorizationResponse;
import com.homecare.service.ClientAuthorizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authorizations")
@CrossOrigin("*")
public class ClientAuthorizationController {

    private final ClientAuthorizationService authorizationService;

    public ClientAuthorizationController(
            ClientAuthorizationService authorizationService
    ) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ClientAuthorizationResponse createAuthorization(
            @RequestBody ClientAuthorizationRequest request
    ) {
        return authorizationService.createAuthorization(request);
    }

    @GetMapping
    public List<ClientAuthorizationResponse> getAllAuthorizations() {
        return authorizationService.getAllAuthorizations();
    }

    @GetMapping("/client/{clientId}")
    public List<ClientAuthorizationResponse> getAuthorizationsByClient(
            @PathVariable Long clientId
    ) {
        return authorizationService.getAuthorizationsByClient(clientId);
    }

    @GetMapping("/expiring-soon")
    public List<ClientAuthorizationResponse> getExpiringSoonAuthorizations() {
        return authorizationService.getExpiringSoonAuthorizations();
    }

    @GetMapping("/expired")
    public List<ClientAuthorizationResponse> getExpiredAuthorizations() {
        return authorizationService.getExpiredAuthorizations();
    }

    @PutMapping("/{id}")
    public ClientAuthorizationResponse updateAuthorization(
            @PathVariable Long id,
            @RequestBody ClientAuthorizationRequest request
    ) {
        return authorizationService.updateAuthorization(id, request);
    }

    @PutMapping("/{id}/close")
    public String closeAuthorization(@PathVariable Long id) {
        authorizationService.closeAuthorization(id);
        return "Authorization closed successfully";
    }
}