package com.homecare.service;

import com.homecare.dto.SchedulerCaregiverResponse;
import com.homecare.dto.WorkforceReadinessResponse;
import com.homecare.entity.Appointment;
import com.homecare.entity.Organization;
import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.CaregiverTimeEntryRepository;
import com.homecare.repository.ClientCaregiverRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CaregiverTimeEntryRepository caregiverTimeEntryRepository;
    private final WorkforceReadinessService workforceReadinessService;
    private final ClientCaregiverRepository clientCaregiverRepository;

    public SchedulerService(
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            CaregiverTimeEntryRepository caregiverTimeEntryRepository,
            WorkforceReadinessService workforceReadinessService,
            ClientCaregiverRepository clientCaregiverRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.caregiverTimeEntryRepository = caregiverTimeEntryRepository;
        this.workforceReadinessService = workforceReadinessService;
        this.clientCaregiverRepository = clientCaregiverRepository;
    }

    public List<SchedulerCaregiverResponse> getSchedulerCaregivers(
            String actorEmail
    ) {
        User actor = getActor(actorEmail);
        Organization organization = requireOrganization(actor);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return userRepository
                .findByOrganizationIdAndRoleOrderByCreatedAtDesc(
                        organization.getId(),
                        Role.CAREGIVER
                )
                .stream()
                .map(caregiver -> buildSchedulerCaregiver(
                        caregiver,
                        organization.getId(),
                        actorEmail,
                        startOfDay,
                        endOfDay
                ))
                .toList();
    }

    private SchedulerCaregiverResponse buildSchedulerCaregiver(
            User caregiver,
            Long organizationId,
            String actorEmail,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    ) {
        WorkforceReadinessResponse readiness =
                workforceReadinessService.getReadiness(
                        caregiver.getId(),
                        actorEmail
                );

        List<Appointment> todayAppointments =
                appointmentRepository.findByCaregiverIdAndOrganizationIdAndStartTimeBetween(
                        caregiver.getId(),
                        organizationId,
                        startOfDay,
                        endOfDay
                );

        double scheduledHoursToday = todayAppointments
                .stream()
                .filter(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()))
                .mapToDouble(this::calculateAppointmentHours)
                .sum();

        boolean currentlyClockedIn =
                caregiverTimeEntryRepository.existsByCaregiverIdAndStatus(
                        caregiver.getId(),
                        "CLOCKED_IN"
                );

        String status = determineSchedulerStatus(readiness, todayAppointments);

        return SchedulerCaregiverResponse.builder()
                .caregiverId(caregiver.getId())
                .caregiverName(caregiver.getFullName())
                .caregiverEmail(caregiver.getEmail())
                .readyToWork(readiness.getReadyToWork())
                .schedulingBlocked(readiness.getSchedulingBlocked())
                .readinessScore(readiness.getReadinessScore())
                .todayAppointments(todayAppointments.size())
                .scheduledHoursToday(scheduledHoursToday)
                .currentlyClockedIn(currentlyClockedIn)
                .blockingReasons(readiness.getBlockingReasons())
                .warnings(readiness.getWarnings())
                .status(status)
                .build();
    }

    private double calculateAppointmentHours(Appointment appointment) {
        if (appointment.getStartTime() == null || appointment.getEndTime() == null) {
            return 0.0;
        }

        long minutes = Duration.between(
                appointment.getStartTime(),
                appointment.getEndTime()
        ).toMinutes();

        return minutes / 60.0;
    }

    private String determineSchedulerStatus(
            WorkforceReadinessResponse readiness,
            List<Appointment> todayAppointments
    ) {
        if (Boolean.TRUE.equals(readiness.getSchedulingBlocked())) {
            return "BLOCKED";
        }

        boolean hasWarnings =
                readiness.getWarnings() != null &&
                        !readiness.getWarnings().isEmpty();

        if (hasWarnings) {
            return "WARNING";
        }

        return "AVAILABLE";
    }

    private User getActor(String actorEmail) {
        return userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));
    }

    private Organization requireOrganization(User user) {
        if (user.getOrganization() == null ||
                user.getOrganization().getId() == null) {
            throw new RuntimeException("User is not assigned to an organization.");
        }

        return user.getOrganization();
    }
}