package com.homecare.service;

import com.homecare.dto.AssignCaregiverRequest;
import com.homecare.dto.ClientCaregiverResponse;
import com.homecare.entity.Client;
import com.homecare.entity.ClientCaregiver;
import com.homecare.entity.User;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientCaregiverService {

    private final ClientCaregiverRepository clientCaregiverRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientCaregiverService(
            ClientCaregiverRepository clientCaregiverRepository,
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.clientCaregiverRepository = clientCaregiverRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public ClientCaregiverResponse assignCaregiver(AssignCaregiverRequest request) {
        if (clientCaregiverRepository.existsByClientIdAndCaregiverIdAndActiveTrue(
                request.getClientId(),
                request.getCaregiverId()
        )) {
            throw new RuntimeException("Caregiver is already assigned to this client.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found."));

        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        ClientCaregiver assignment = ClientCaregiver.builder()
                .client(client)
                .caregiver(caregiver)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .primaryCaregiver(Boolean.TRUE.equals(request.getPrimaryCaregiver()))
                .active(true)
                .build();

        return mapToResponse(clientCaregiverRepository.save(assignment));
    }

    public List<ClientCaregiverResponse> getCaregiversByClient(Long clientId) {
        return clientCaregiverRepository.findByClientIdAndActiveTrue(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClientCaregiverResponse deactivateAssignment(Long id) {
        ClientCaregiver assignment = clientCaregiverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caregiver assignment not found."));

        assignment.setActive(false);

        return mapToResponse(clientCaregiverRepository.save(assignment));
    }

    private ClientCaregiverResponse mapToResponse(ClientCaregiver assignment) {
        return ClientCaregiverResponse.builder()
                .id(assignment.getId())
                .clientId(assignment.getClient().getId())
                .clientName(assignment.getClient().getFullName())
                .caregiverId(assignment.getCaregiver().getId())
                .caregiverName(assignment.getCaregiver().getFullName())
                .caregiverEmail(assignment.getCaregiver().getEmail())
                .caregiverRole(assignment.getCaregiver().getRole().name())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .active(assignment.getActive())
                .primaryCaregiver(assignment.getPrimaryCaregiver())
                .build();
    }
}