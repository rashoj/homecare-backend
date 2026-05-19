package com.homecare.service;

import com.homecare.dto.ClientRequest;
import com.homecare.dto.ClientResponse;
import com.homecare.entity.Client;
import com.homecare.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ClientResponse createClient(ClientRequest request) {

        Client client = Client.builder()
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .allergies(request.getAllergies())
                .medicalConditions(request.getMedicalConditions())
                .carePlan(request.getCarePlan())
                .mobilityStatus(request.getMobilityStatus())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .active(true)
                .build();

        return mapToResponse(clientRepository.save(client));
    }

    public List<ClientResponse> getAllClients() {
        return clientRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        return mapToResponse(client);
    }

    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setFullName(request.getFullName());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setGender(request.getGender());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setAddress(request.getAddress());
        client.setEmergencyContactName(request.getEmergencyContactName());
        client.setEmergencyContactPhone(request.getEmergencyContactPhone());
        client.setAllergies(request.getAllergies());
        client.setMedicalConditions(request.getMedicalConditions());
        client.setCarePlan(request.getCarePlan());
        client.setMobilityStatus(request.getMobilityStatus());
        client.setLatitude(request.getLatitude());
        client.setLongitude(request.getLongitude());

        return mapToResponse(clientRepository.save(client));
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setActive(false);
        clientRepository.save(client);
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .dateOfBirth(client.getDateOfBirth())
                .gender(client.getGender())
                .phoneNumber(client.getPhoneNumber())
                .address(client.getAddress())
                .emergencyContactName(client.getEmergencyContactName())
                .emergencyContactPhone(client.getEmergencyContactPhone())
                .allergies(client.getAllergies())
                .medicalConditions(client.getMedicalConditions())
                .carePlan(client.getCarePlan())
                .mobilityStatus(client.getMobilityStatus())
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .active(client.getActive())
                .build();
    }
}