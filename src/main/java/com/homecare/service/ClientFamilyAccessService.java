package com.homecare.service;

import com.homecare.dto.ClientFamilyAccessRequest;
import com.homecare.entity.Client;
import com.homecare.entity.ClientFamilyAccess;
import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.ClientFamilyAccessRepository;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientFamilyAccessService {

    private final ClientFamilyAccessRepository accessRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public ClientFamilyAccessService(
            ClientFamilyAccessRepository accessRepository,
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.accessRepository = accessRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public ClientFamilyAccess createAccess(ClientFamilyAccessRequest request) {
        if (accessRepository.existsByClientIdAndFamilyUserIdAndActiveTrue(
                request.getClientId(),
                request.getFamilyUserId()
        )) {
            throw new RuntimeException("Family user already has access to this client.");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found."));

        User familyUser = userRepository.findById(request.getFamilyUserId())
                .orElseThrow(() -> new RuntimeException("Family user not found."));

        if (familyUser.getRole() != Role.FAMILY_MEMBER) {
            throw new RuntimeException("Selected user is not a family member.");
        }

        ClientFamilyAccess access = ClientFamilyAccess.builder()
                .client(client)
                .familyUser(familyUser)
                .relationship(request.getRelationship())
                .active(true)
                .build();

        return accessRepository.save(access);
    }
}