package com.homecare.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testApi() {
        return "HomeCare backend is running successfully!";
    }

    @GetMapping("/api/secure-test")
    public String secureTest() {
        return "This is a protected API. JWT is working!";
    }
}