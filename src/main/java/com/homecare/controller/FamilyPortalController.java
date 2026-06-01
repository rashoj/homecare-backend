package com.homecare.controller;

import com.homecare.dto.*;
import com.homecare.service.FamilyPortalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/family-portal")
@CrossOrigin("*")
public class FamilyPortalController {

    private final FamilyPortalService familyPortalService;

    public FamilyPortalController(FamilyPortalService familyPortalService) {
        this.familyPortalService = familyPortalService;
    }

    @GetMapping("/dashboard")
    public FamilyDashboardResponse getDashboard(Authentication authentication) {
        return familyPortalService.getDashboard(authentication.getName());
    }

    @GetMapping("/appointments")
    public List<FamilyAppointmentResponse> getAppointments(
            Authentication authentication
    ) {
        return familyPortalService.getAppointments(authentication.getName());
    }

    @GetMapping("/visit-notes")
    public List<FamilyVisitNoteResponse> getVisitNotes(
            Authentication authentication
    ) {
        return familyPortalService.getVisitNotes(authentication.getName());
    }

    @GetMapping("/documents")
    public List<FamilyDocumentResponse> getDocuments(
            Authentication authentication
    ) {
        return familyPortalService.getDocuments(authentication.getName());
    }

    @PostMapping("/documents/upload")
    public DocumentResponse uploadFamilyDocument(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "expirationDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate
    ) {
        return familyPortalService.uploadFamilyDocument(
                authentication.getName(),
                file,
                documentName,
                documentType,
                expirationDate
        );
    }

    @GetMapping("/medications")
    public List<FamilyMedicationResponse> getMedications(
            Authentication authentication
    ) {
        return familyPortalService.getMedications(authentication.getName());
    }

    @GetMapping("/care-team")
    public List<ClientCaregiverResponse> getCareTeam(
            Authentication authentication
    ) {
        return familyPortalService.getCareTeam(authentication.getName());
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> getNotifications(
            Authentication authentication
    ) {
        return familyPortalService.getNotifications(authentication.getName());
    }

    @GetMapping("/notifications/unread-count")
    public Long getUnreadCount(
            Authentication authentication
    ) {
        return familyPortalService.getUnreadCount(authentication.getName());
    }

    @PutMapping("/notifications/{id}/read")
    public NotificationResponse markNotificationRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return familyPortalService.markNotificationRead(
                id,
                authentication.getName()
        );
    }

    @GetMapping("/timeline")
    public List<FamilyTimelineItemResponse> getTimeline(
            Authentication authentication
    ) {
        return familyPortalService.getTimeline(authentication.getName());
    }

    @GetMapping("/messages/conversations")
    public List<ConversationResponse> getFamilyConversations(
            Authentication authentication
    ) {
        return familyPortalService.getFamilyConversations(authentication.getName());
    }

    @PostMapping("/messages/conversations")
    public ConversationResponse createFamilyConversation(
            Authentication authentication,
            @RequestBody CreateConversationRequest request
    ) {
        return familyPortalService.createFamilyConversation(
                authentication.getName(),
                request
        );
    }

    @GetMapping("/messages/conversations/{conversationId}")
    public List<MessageResponse> getFamilyMessages(
            Authentication authentication,
            @PathVariable Long conversationId
    ) {
        return familyPortalService.getFamilyMessages(
                authentication.getName(),
                conversationId
        );
    }

    @PostMapping("/messages/conversations/{conversationId}")
    public MessageResponse sendFamilyMessage(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestBody SendMessageRequest request
    ) {
        return familyPortalService.sendFamilyMessage(
                authentication.getName(),
                conversationId,
                request
        );
    }
}