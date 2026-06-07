package com.homecare.controller;

import com.homecare.dto.ClientRequest;
import com.homecare.dto.ClientResponse;
import com.homecare.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin("*")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ClientResponse createClient(
            @Valid @RequestBody ClientRequest request,
            Authentication authentication
    ) {
        return clientService.createClient(request, authentication.getName());
    }

    @GetMapping
    public List<ClientResponse> getAllClients(Authentication authentication) {
        return clientService.getAllClients(authentication.getName());
    }

    @GetMapping("/{id}")
    public ClientResponse getClientById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return clientService.getClientById(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public ClientResponse updateClient(
            @PathVariable Long id,
            @RequestBody ClientRequest request,
            Authentication authentication
    ) {
        return clientService.updateClient(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public String deleteClient(
            @PathVariable Long id,
            Authentication authentication
    ) {
        clientService.deleteClient(id, authentication.getName());
        return "Client deleted successfully";
    }
}