package com.homecare.controller;

import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.homecare.entity.Role;

import java.util.List;

@RestController
@RequestMapping("/api/caregivers")
@CrossOrigin("*")
public class CaregiverController {

    private final UserRepository userRepository;

    public CaregiverController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getCaregivers() {
        return userRepository.findByRole(Role.CAREGIVER);
    }
    @GetMapping("/debug/me")
    public Object debugMe(Authentication authentication) {
        return java.util.Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}