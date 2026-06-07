package com.homecare.service;

import com.homecare.dto.ClientRequest;
import com.homecare.dto.ClientResponse;
import com.homecare.entity.Client;
import com.homecare.entity.Organization;
import com.homecare.entity.User;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientService(
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public ClientResponse createClient(ClientRequest request, String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

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
                .organization(organization)
                .active(true)
                .build();

        return mapToResponse(clientRepository.save(client));
    }

    public List<ClientResponse> getAllClients(String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        return clientRepository.findByOrganizationIdAndActiveTrue(organization.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClientResponse getClientById(Long id, String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Client client = clientRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new RuntimeException("Client not found for this organization"));

        return mapToResponse(client);
    }

    public ClientResponse updateClient(Long id, ClientRequest request, String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Client client = clientRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new RuntimeException("Client not found for this organization"));

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

    public void deleteClient(Long id, String actorEmail) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        Client client = clientRepository.findByIdAndOrganizationId(id, organization.getId())
                .orElseThrow(() -> new RuntimeException("Client not found for this organization"));

        client.setActive(false);
        clientRepository.save(client);
    }

    private User getActor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new RuntimeException(
                    "User is not assigned to an organization. userId="
                            + user.getId()
                            + ", email="
                            + user.getEmail()
                            + ", role="
                            + user.getRole()
            );
        }

        return user.getOrganization();
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