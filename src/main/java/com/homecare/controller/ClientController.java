package com.homecare.controller;

import com.homecare.dto.ClientRequest;
import com.homecare.dto.ClientResponse;
import com.homecare.service.ClientService;
import jakarta.validation.Valid;
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
    public ClientResponse createClient(@Valid @RequestBody ClientRequest request) {
        return clientService.createClient(request);
    }

    @GetMapping
    public List<ClientResponse> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    public ClientResponse getClientById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }

    @PutMapping("/{id}")
    public ClientResponse updateClient(@PathVariable Long id,
                                       @RequestBody ClientRequest request) {
        return clientService.updateClient(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "Client deleted successfully";
    }

}