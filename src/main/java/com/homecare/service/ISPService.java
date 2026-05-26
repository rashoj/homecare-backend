package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ISPService {

    private final ISPPlanRepository ispPlanRepository;
    private final ISPGoalRepository ispGoalRepository;
    private final ISPGoalProgressLogRepository progressLogRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;

    public ISPService(
            ISPPlanRepository ispPlanRepository,
            ISPGoalRepository ispGoalRepository,
            ISPGoalProgressLogRepository progressLogRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ServiceDocumentationRepository serviceDocumentationRepository
    ) {
        this.ispPlanRepository = ispPlanRepository;
        this.ispGoalRepository = ispGoalRepository;
        this.progressLogRepository = progressLogRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
    }

    public ISPPlanResponse createPlan(ISPPlanRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found."));

        ISPPlan plan = ISPPlan.builder()
                .client(client)
                .planName(request.getPlanName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("ACTIVE")
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        return mapPlan(ispPlanRepository.save(plan));
    }

    public ISPGoalResponse createGoal(ISPGoalRequest request) {
        ISPPlan plan = ispPlanRepository.findById(request.getIspPlanId())
                .orElseThrow(() -> new RuntimeException("ISP plan not found."));

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found."));

        ISPGoal goal = ISPGoal.builder()
                .ispPlan(plan)
                .client(client)
                .goalTitle(request.getGoalTitle())
                .goalDescription(request.getGoalDescription())
                .category(request.getCategory())
                .targetDate(request.getTargetDate())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        return mapGoal(ispGoalRepository.save(goal));
    }

    public ISPGoalProgressResponse submitProgress(ISPGoalProgressRequest request) {
        ISPGoal goal = ispGoalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new RuntimeException("ISP goal not found."));

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found."));

        User caregiver = userRepository.findById(request.getCaregiverId())
                .orElseThrow(() -> new RuntimeException("Caregiver not found."));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        ServiceDocumentation documentation = serviceDocumentationRepository
                .findById(request.getServiceDocumentationId())
                .orElseThrow(() -> new RuntimeException("Service documentation not found."));

        ISPGoalProgressLog log = ISPGoalProgressLog.builder()
                .goal(goal)
                .client(client)
                .caregiver(caregiver)
                .appointment(appointment)
                .serviceDocumentation(documentation)
                .progressStatus(request.getProgressStatus())
                .promptLevel(request.getPromptLevel())
                .progressNote(request.getProgressNote())
                .createdAt(LocalDateTime.now())
                .build();

        return mapProgress(progressLogRepository.save(log));
    }

    public List<ISPPlanResponse> getPlansByClient(Long clientId) {
        return ispPlanRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapPlan)
                .toList();
    }

    public List<ISPGoalResponse> getActiveGoalsByClient(Long clientId) {
        return ispGoalRepository.findByClientIdAndStatusOrderByCreatedAtDesc(clientId, "ACTIVE")
                .stream()
                .map(this::mapGoal)
                .toList();
    }

    public List<ISPGoalProgressResponse> getProgressByClient(Long clientId) {
        return progressLogRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapProgress)
                .toList();
    }

    public List<ISPGoalProgressResponse> getProgressByDocumentation(Long documentationId) {
        return progressLogRepository.findByServiceDocumentationIdOrderByCreatedAtDesc(documentationId)
                .stream()
                .map(this::mapProgress)
                .toList();
    }

    private ISPPlanResponse mapPlan(ISPPlan plan) {
        return ISPPlanResponse.builder()
                .id(plan.getId())
                .clientId(plan.getClient().getId())
                .clientName(plan.getClient().getFullName())
                .planName(plan.getPlanName())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .status(plan.getStatus())
                .notes(plan.getNotes())
                .createdAt(plan.getCreatedAt())
                .build();
    }

    private ISPGoalResponse mapGoal(ISPGoal goal) {
        return ISPGoalResponse.builder()
                .id(goal.getId())
                .ispPlanId(goal.getIspPlan().getId())
                .clientId(goal.getClient().getId())
                .clientName(goal.getClient().getFullName())
                .goalTitle(goal.getGoalTitle())
                .goalDescription(goal.getGoalDescription())
                .category(goal.getCategory())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .build();
    }

    private ISPGoalProgressResponse mapProgress(ISPGoalProgressLog log) {
        return ISPGoalProgressResponse.builder()
                .id(log.getId())
                .goalId(log.getGoal().getId())
                .goalTitle(log.getGoal().getGoalTitle())
                .clientId(log.getClient().getId())
                .clientName(log.getClient().getFullName())
                .caregiverId(log.getCaregiver().getId())
                .caregiverName(log.getCaregiver().getFullName())
                .appointmentId(log.getAppointment().getId())
                .serviceDocumentationId(log.getServiceDocumentation().getId())
                .progressStatus(log.getProgressStatus())
                .promptLevel(log.getPromptLevel())
                .progressNote(log.getProgressNote())
                .createdAt(log.getCreatedAt())
                .build();
    }
}