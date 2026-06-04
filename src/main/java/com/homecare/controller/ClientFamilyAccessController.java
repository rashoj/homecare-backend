package com.homecare.controller;

import com.homecare.dto.ClientFamilyAccessRequest;
import com.homecare.entity.ClientFamilyAccess;
import com.homecare.service.ClientFamilyAccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client-family-access")
@CrossOrigin("*")
public class ClientFamilyAccessController {

    private final ClientFamilyAccessService service;

    public ClientFamilyAccessController(ClientFamilyAccessService service) {
        this.service = service;
    }

    @PostMapping
    public ClientFamilyAccess createAccess(
            @RequestBody ClientFamilyAccessRequest request
    ) {
        return service.createAccess(request);
    }
}