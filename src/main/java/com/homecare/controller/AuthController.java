package com.homecare.controller;

import com.homecare.dto.AuthResponse;
import com.homecare.dto.LoginRequest;
import com.homecare.dto.RegisterRequest;
import com.homecare.service.AuthService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/reset-admin-password")
    public String resetAdminPassword() {
        authService.resetAdminPassword();
        return "Admin password reset successfully";
    }
}