package com.homecare.controller;

import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/caregivers")
@CrossOrigin("*")
public class CaregiverController {

    private final UserRepository userRepository;

    public CaregiverController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getCaregivers(Authentication authentication) {
        User actor = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("Logged-in user is not assigned to an organization.");
        }

        return userRepository
                .findByOrganizationIdAndRoleOrderByCreatedAtDesc(
                        actor.getOrganization().getId(),
                        Role.CAREGIVER
                );
    }

    @GetMapping("/debug/me")
    public Object debugMe(Authentication authentication) {
        User actor = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        return Map.of(
                "name", authentication.getName(),
                "role", actor.getRole(),
                "organizationId", actor.getOrganization() != null
                        ? actor.getOrganization().getId()
                        : null,
                "authorities", authentication.getAuthorities()
        );
    }
}