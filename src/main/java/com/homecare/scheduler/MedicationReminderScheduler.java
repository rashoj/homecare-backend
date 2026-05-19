package com.homecare.scheduler;

import com.homecare.dto.NotificationRequest;
import com.homecare.entity.Medication;
import com.homecare.entity.User;
import com.homecare.repository.MedicationRepository;
import com.homecare.repository.UserRepository;
import com.homecare.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
public class MedicationReminderScheduler {

    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MedicationReminderScheduler(
            MedicationRepository medicationRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.medicationRepository = medicationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 300000)
    public void sendMedicationReminders() {

        LocalTime now = LocalTime.now();

        List<Medication> medications = medicationRepository.findByActiveTrue();

        for (Medication medication : medications) {

            LocalTime scheduled = medication.getScheduledTime();

            if (scheduled == null) {
                continue;
            }

            boolean withinReminderWindow =
                    Math.abs(now.toSecondOfDay() - scheduled.toSecondOfDay()) <= 300;

            if (withinReminderWindow) {

                List<User> caregivers = userRepository.findAll()
                        .stream()
                        .filter(user ->
                                user.getRole().name().equals("CAREGIVER"))
                        .toList();

                for (User caregiver : caregivers) {

                    NotificationRequest request = new NotificationRequest();

                    request.setUserId(caregiver.getId());

                    request.setTitle("Medication Reminder");

                    request.setMessage(
                            medication.getClient().getFullName()
                                    + " medication "
                                    + medication.getMedicationName()
                                    + " is due now."
                    );

                    request.setType("MEDICATION_REMINDER");

                    request.setRelatedEntityType("MEDICATION");

                    request.setRelatedEntityId(medication.getId());

                    notificationService.createNotification(request);
                }
            }
        }
    }
}