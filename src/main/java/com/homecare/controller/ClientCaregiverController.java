package com.homecare.controller;

import com.homecare.dto.AssignCaregiverRequest;
import com.homecare.dto.ClientCaregiverResponse;
import com.homecare.service.ClientCaregiverService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client-caregivers")
@CrossOrigin("*")
public class ClientCaregiverController {

    private final ClientCaregiverService clientCaregiverService;

    public ClientCaregiverController(ClientCaregiverService clientCaregiverService) {
        this.clientCaregiverService = clientCaregiverService;
    }

    @PostMapping
    public ClientCaregiverResponse assignCaregiver(
            @RequestBody AssignCaregiverRequest request
    ) {
        return clientCaregiverService.assignCaregiver(request);
    }

    @GetMapping("/client/{clientId}")
    public List<ClientCaregiverResponse> getCaregiversByClient(
            @PathVariable Long clientId
    ) {
        return clientCaregiverService.getCaregiversByClient(clientId);
    }

    @PutMapping("/{id}/deactivate")
    public ClientCaregiverResponse deactivateAssignment(
            @PathVariable Long id
    ) {
        return clientCaregiverService.deactivateAssignment(id);
    }

}