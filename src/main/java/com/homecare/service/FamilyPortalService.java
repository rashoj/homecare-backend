package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.ClientFamilyAccess;
import com.homecare.entity.Notification;
import com.homecare.entity.User;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.homecare.dto.FamilyMedicationResponse;
import com.homecare.dto.ClientCaregiverResponse;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.service.MessagingService;
import com.homecare.dto.ConversationResponse;
import com.homecare.dto.CreateConversationRequest;
import com.homecare.dto.MessageResponse;
import com.homecare.dto.SendMessageRequest;
import com.homecare.dto.FamilyTimelineItemResponse;
import java.util.ArrayList;
import java.util.Comparator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FamilyPortalService {

    private final UserRepository userRepository;
    private final ClientFamilyAccessRepository accessRepository;
    private final AppointmentRepository appointmentRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final DocumentRepository documentRepository;
    private final MedicationRepository medicationRepository;
    private final DocumentService documentService;
    private final ClientCaregiverRepository clientCaregiverRepository;
    private final NotificationRepository notificationRepository;
    private final MessagingService messagingService;

    public FamilyPortalService(
            UserRepository userRepository,
            ClientFamilyAccessRepository accessRepository,
            AppointmentRepository appointmentRepository,
            VisitNoteRepository visitNoteRepository,
            DocumentRepository documentRepository,
            MedicationRepository medicationRepository,DocumentService documentService,ClientCaregiverRepository clientCaregiverRepository,NotificationRepository notificationRepository,MessagingService messagingService
    ) {
        this.userRepository = userRepository;
        this.accessRepository = accessRepository;
        this.appointmentRepository = appointmentRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.documentRepository = documentRepository;
        this.medicationRepository = medicationRepository;
        this.documentService = documentService;
        this.clientCaregiverRepository = clientCaregiverRepository;
        this.notificationRepository = notificationRepository;
        this.messagingService = messagingService;
    }

    public FamilyDashboardResponse getDashboard(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned to this family user."));

        Long clientId = access.getClient().getId();

        long upcomingAppointments = appointmentRepository.findAll()
                .stream()
                .filter(a -> a.getClient() != null && a.getClient().getId().equals(clientId))
                .filter(a -> !Boolean.TRUE.equals(a.getCompleted()))
                .count();

        long completedVisits = appointmentRepository.findAll()
                .stream()
                .filter(a -> a.getClient() != null && a.getClient().getId().equals(clientId))
                .filter(a -> Boolean.TRUE.equals(a.getCompleted()))
                .count();

        return FamilyDashboardResponse.builder()
                .familyUserId(familyUser.getId())
                .familyName(familyUser.getFullName())
                .clientId(access.getClient().getId())
                .clientName(access.getClient().getFullName())
                .upcomingAppointments(upcomingAppointments)
                .completedVisits(completedVisits)
                .recentVisitNotes((long) visitNoteRepository.findByClientId(clientId).size())
                .sharedDocuments((long) documentRepository.findByClientId(clientId).size())
                .activeMedications((long) medicationRepository.findByClientId(clientId).size())
                .build();
    }
    public List<FamilyAppointmentResponse> getAppointments(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        Long clientId = access.getClient().getId();

        return appointmentRepository
                .findByClientIdOrderByStartTimeDesc(clientId)
                .stream()
                .map(appointment -> FamilyAppointmentResponse.builder()
                        .id(appointment.getId())
                        .clientId(appointment.getClient().getId())
                        .clientName(appointment.getClient().getFullName())
                        .caregiverId(
                                appointment.getCaregiver() != null
                                        ? appointment.getCaregiver().getId()
                                        : null
                        )
                        .caregiverName(
                                appointment.getCaregiver() != null
                                        ? appointment.getCaregiver().getFullName()
                                        : "Not assigned"
                        )
                        .startTime(appointment.getStartTime())
                        .endTime(appointment.getEndTime())
                        .status(appointment.getStatus())
                        .completed(appointment.getCompleted())
                        .build())
                .toList();
    }
    public List<FamilyVisitNoteResponse> getVisitNotes(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        Long clientId = access.getClient().getId();

        return visitNoteRepository.findByClientId(clientId)
                .stream()
                .map(note -> FamilyVisitNoteResponse.builder()
                        .id(note.getId())
                        .appointmentId(note.getAppointment().getId())
                        .caregiverName(note.getCaregiver().getFullName())
                        .generalNotes(note.getGeneralNotes())
                        .meals(note.getMeals())
                        .medicationNotes(note.getMedicationNotes())
                        .mobilityNotes(note.getMobilityNotes())
                        .moodNotes(note.getMoodNotes())
                        .hygieneCare(note.getHygieneCare())
                        .safetyConcerns(note.getSafetyConcerns())
                        .familyUpdate(note.getFamilyUpdate())
                        .aiSummary(note.getAiSummary())
                        .createdAt(note.getCreatedAt())
                        .build())
                .toList();
    }
    public List<FamilyDocumentResponse> getDocuments(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        Long clientId = access.getClient().getId();

        return documentRepository.findByClientId(clientId)
                .stream()
                .filter(document -> "APPROVED".equalsIgnoreCase(document.getApprovalStatus()))
                .map(document -> FamilyDocumentResponse.builder()
                        .id(document.getId())
                        .documentName(document.getDocumentName())
                        .documentType(document.getDocumentType())
                        .fileName(document.getFileName())
                        .contentType(document.getContentType())
                        .expirationDate(document.getExpirationDate())
                        .approvalStatus(document.getApprovalStatus())
                        .uploadedAt(document.getUploadedAt())
                        .build())
                .toList();
    }
    public DocumentResponse uploadFamilyDocument(
            String email,
            MultipartFile file,
            String documentName,
            String documentType,
            LocalDate expirationDate
    ) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        return documentService.uploadDocument(
                file,
                documentName,
                documentType,
                familyUser.getId(),
                access.getClient().getId(),
                expirationDate
        );
    }
    public List<FamilyMedicationResponse> getMedications(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        Long clientId = access.getClient().getId();

        return medicationRepository.findByClientIdAndActiveTrue(clientId)
                .stream()
                .map(medication -> FamilyMedicationResponse.builder()
                        .id(medication.getId())
                        .medicationName(medication.getMedicationName())
                        .dosage(medication.getDosage())
                        .frequency(medication.getFrequency())
                        .scheduledTime(medication.getScheduledTime())
                        .instructions(medication.getInstructions())
                        .active(medication.getActive())
                        .build())
                .toList();
    }
    public List<ClientCaregiverResponse> getCareTeam(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        Long clientId = access.getClient().getId();

        return clientCaregiverRepository.findByClientIdAndActiveTrue(clientId)
                .stream()
                .map(assignment -> ClientCaregiverResponse.builder()
                        .id(assignment.getId())
                        .clientId(assignment.getClient().getId())
                        .clientName(assignment.getClient().getFullName())
                        .caregiverId(assignment.getCaregiver().getId())
                        .caregiverName(assignment.getCaregiver().getFullName())
                        .caregiverEmail(assignment.getCaregiver().getEmail())
                        .caregiverRole(assignment.getCaregiver().getRole().name())
                        .startDate(assignment.getStartDate())
                        .endDate(assignment.getEndDate())
                        .active(assignment.getActive())
                        .primaryCaregiver(assignment.getPrimaryCaregiver())
                        .build())
                .toList();
    }
    public List<NotificationResponse> getNotifications(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(familyUser.getId())
                .stream()
                .map(this::mapNotification)
                .toList();
    }

    public Long getUnreadCount(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        return notificationRepository
                .countByUserIdAndIsReadFalse(familyUser.getId());
    }

    public NotificationResponse markNotificationRead(
            Long notificationId,
            String email
    ) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(familyUser.getId())) {
            throw new RuntimeException("Not authorized to access notification");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        return mapNotification(notificationRepository.save(notification));
    }

    private NotificationResponse mapNotification(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .userName(notification.getUser().getFullName())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
    public List<FamilyTimelineItemResponse> getTimeline(String email) {
        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        Long clientId = access.getClient().getId();

        List<FamilyTimelineItemResponse> timeline = new ArrayList<>();

        notificationRepository.findByUserIdOrderByCreatedAtDesc(familyUser.getId())
                .forEach(notification -> timeline.add(
                        FamilyTimelineItemResponse.builder()
                                .type("NOTIFICATION")
                                .title(notification.getTitle())
                                .description(notification.getMessage())
                                .timestamp(notification.getCreatedAt())
                                .relatedEntityType(notification.getRelatedEntityType())
                                .relatedEntityId(notification.getRelatedEntityId())
                                .build()
                ));

        visitNoteRepository.findByClientId(clientId)
                .forEach(note -> timeline.add(
                        FamilyTimelineItemResponse.builder()
                                .type("VISIT_NOTE")
                                .title("Visit Note Added")
                                .description(
                                        note.getFamilyUpdate() != null
                                                ? note.getFamilyUpdate()
                                                : note.getGeneralNotes()
                                )
                                .timestamp(note.getCreatedAt())
                                .relatedEntityType("VISIT_NOTE")
                                .relatedEntityId(note.getId())
                                .build()
                ));

        documentRepository.findByClientId(clientId)
                .forEach(document -> timeline.add(
                        FamilyTimelineItemResponse.builder()
                                .type("DOCUMENT")
                                .title("Document " + document.getApprovalStatus())
                                .description(document.getDocumentName())
                                .timestamp(document.getUploadedAt())
                                .relatedEntityType("DOCUMENT")
                                .relatedEntityId(document.getId())
                                .build()
                ));

        medicationRepository.findByClientIdAndActiveTrue(clientId)
                .forEach(medication -> timeline.add(
                        FamilyTimelineItemResponse.builder()
                                .type("MEDICATION")
                                .title("Medication Active")
                                .description(
                                        medication.getMedicationName()
                                                + " - "
                                                + medication.getDosage()
                                )
                                .timestamp(medication.getCreatedAt())
                                .relatedEntityType("MEDICATION")
                                .relatedEntityId(medication.getId())
                                .build()
                ));

        clientCaregiverRepository.findByClientIdAndActiveTrue(clientId)
                .forEach(assignment -> timeline.add(
                        FamilyTimelineItemResponse.builder()
                                .type("CARE_TEAM")
                                .title("Caregiver Assigned")
                                .description(assignment.getCaregiver().getFullName())
                                .timestamp(assignment.getCreatedAt())
                                .relatedEntityType("CLIENT_CAREGIVER")
                                .relatedEntityId(assignment.getId())
                                .build()
                ));

        return timeline.stream()
                .filter(item -> item.getTimestamp() != null)
                .sorted(Comparator.comparing(
                        FamilyTimelineItemResponse::getTimestamp
                ).reversed())
                .limit(25)
                .toList();
    }
    public List<ConversationResponse> getFamilyConversations(String email) {

        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        return messagingService.getConversationsByUser(
                familyUser.getId()
        );
    }

    public ConversationResponse createFamilyConversation(
            String email,
            CreateConversationRequest request
    ) {

        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        ClientFamilyAccess access = accessRepository
                .findByFamilyUserIdAndActiveTrue(familyUser.getId())
                .orElseThrow(() -> new RuntimeException("No client access assigned."));

        request.setCreatedByUserId(familyUser.getId());
        request.setClientId(access.getClient().getId());

        if (request.getType() == null || request.getType().isBlank()) {
            request.setType("FAMILY_AGENCY");
        }
        ConversationResponse conversation =
                messagingService.createConversation(request);
        messagingService.addParticipant(conversation.getId(), 2L);

        return conversation;
    }

    public List<MessageResponse> getFamilyMessages(
            String email,
            Long conversationId
    ) {

        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        return messagingService.getMessages(
                conversationId,
                familyUser.getId()
        );
    }

    public MessageResponse sendFamilyMessage(
            String email,
            Long conversationId,
            SendMessageRequest request
    ) {

        User familyUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Family user not found"));

        request.setConversationId(conversationId);
        request.setSenderUserId(familyUser.getId());

        return messagingService.sendMessage(request);
    }
}