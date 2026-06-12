package com.homecare.service;

import com.homecare.entity.Organization;
import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user, String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("Logged-in user is not assigned to an organization.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        Organization organization = actor.getOrganization();

        user.setId(null);
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setOrganization(organization);
        user.setActive(true);

        if (user.getRole() == null) {
            user.setRole(Role.CAREGIVER);
        }

        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers(String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("Logged-in user is not assigned to an organization.");
        }

        return userRepository.findByOrganizationIdOrderByCreatedAtDesc(
                actor.getOrganization().getId()
        );
    }

    public User getUserById(Long id, String actorEmail) {
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        if (actor.getOrganization() == null ||
                actor.getOrganization().getId() == null) {
            throw new RuntimeException("Logged-in user is not assigned to an organization.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOrganization() == null ||
                !user.getOrganization().getId().equals(actor.getOrganization().getId())) {
            throw new RuntimeException("User does not belong to this organization.");
        }

        return user;
    }
}