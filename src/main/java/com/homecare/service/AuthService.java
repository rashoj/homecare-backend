package com.homecare.service;

import com.homecare.dto.AuthResponse;
import com.homecare.dto.LoginRequest;
import com.homecare.dto.RegisterRequest;
import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import com.homecare.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return AuthResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .token(token)
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail() != null
                ? request.getEmail().trim().toLowerCase()
                : "";

        String rawPassword = request.getPassword() != null
                ? request.getPassword()
                : "";

        System.out.println("========== LOGIN DEBUG ==========");
        System.out.println("LOGIN EMAIL=[" + email + "]");
        System.out.println("LOGIN PASSWORD LENGTH=" + rawPassword.length());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("LOGIN USER NOT FOUND FOR EMAIL=[" + email + "]");
                    return new RuntimeException("Invalid email or password");
                });

        System.out.println("DB USER FOUND=[" + user.getEmail() + "]");
        System.out.println("DB ROLE=[" + user.getRole() + "]");
        System.out.println("DB ACTIVE=[" + user.getActive() + "]");
        System.out.println("DB HASH LENGTH=" + (user.getPassword() != null ? user.getPassword().length() : null));
        System.out.println("DB HASH START=" +
                (user.getPassword() != null && user.getPassword().length() >= 7
                        ? user.getPassword().substring(0, 7)
                        : user.getPassword())
        );

        boolean passwordMatches =
                passwordEncoder.matches(rawPassword, user.getPassword());

        System.out.println("PASSWORD MATCH=" + passwordMatches);
        System.out.println("=================================");

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("User account is inactive");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return AuthResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .message("Login successful")
                .build();
    }

    public void resetAdminPassword() {
        User user = userRepository.findByEmail("admin@homecare.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        user.setPassword(passwordEncoder.encode("admin123"));

        userRepository.save(user);
    }
}