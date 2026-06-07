package com.homecare.controller;

import com.homecare.dto.ContactRequestCreateRequest;
import com.homecare.dto.ContactRequestResponse;
import com.homecare.service.ContactRequestService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ContactRequestController {

    private final ContactRequestService contactRequestService;

    public ContactRequestController(ContactRequestService contactRequestService) {
        this.contactRequestService = contactRequestService;
    }

    @PostMapping("/public/contact-requests")
    public ContactRequestResponse createContactRequest(
            @RequestBody ContactRequestCreateRequest request
    ) {
        return contactRequestService.createContactRequest(request);
    }

    @GetMapping("/platform/contact-requests")
    public List<ContactRequestResponse> getContactRequests() {
        return contactRequestService.getAllContactRequests();
    }

    @PutMapping("/platform/contact-requests/{id}/status")
    public ContactRequestResponse updateContactRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication
    ) {
        return contactRequestService.updateStatus(
                id,
                status,
                authentication.getName()
        );
    }
}