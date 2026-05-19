package com.homecare.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin("*")
public class AuthDebugController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}