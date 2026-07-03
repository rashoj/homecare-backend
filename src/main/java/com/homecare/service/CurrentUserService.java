package com.homecare.service;

import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authenticated user not found.");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    public Long getCurrentOrganizationId() {
        User user = getCurrentUser();

        if (user.getOrganization() == null || user.getOrganization().getId() == null) {
            throw new RuntimeException("Authenticated user is not assigned to an organization.");
        }

        return user.getOrganization().getId();
    }
}