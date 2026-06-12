package com.homecare.controller;

import com.homecare.entity.User;
import com.homecare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(
            @RequestBody User user,
            Authentication authentication
    ) {
        return userService.createUser(user, authentication.getName());
    }

    @GetMapping
    public List<User> getAllUsers(Authentication authentication) {
        return userService.getAllUsers(authentication.getName());
    }

    @GetMapping("/{id}")
    public User getUserById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return userService.getUserById(id, authentication.getName());
    }
}