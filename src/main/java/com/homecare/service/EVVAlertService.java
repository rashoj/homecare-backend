package com.homecare.service;

import com.homecare.entity.EVVAlert;
import com.homecare.entity.EVVException;
import com.homecare.repository.EVVAlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EVVAlertService {

    private final EVVAlertRepository evvAlertRepository;

    public EVVAlertService(EVVAlertRepository evvAlertRepository) {
        this.evvAlertRepository = evvAlertRepository;
    }

    public void createAlertFromException(EVVException exception) {
        EVVAlert alert = EVVAlert.builder()
                .exceptionId(exception.getId())
                .clientId(exception.getClient().getId())
                .caregiverId(exception.getCaregiver().getId())
                .appointmentId(exception.getAppointment().getId())
                .alertType(exception.getExceptionType())
                .severity(exception.getSeverity())
                .status("UNREAD")
                .message(buildMessage(exception))
                .createdAt(LocalDateTime.now())
                .build();

        evvAlertRepository.save(alert);
    }

    public List<EVVAlert> getAllAlerts() {
        return evvAlertRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<EVVAlert> getUnreadAlerts() {
        return evvAlertRepository.findByStatusOrderByCreatedAtDesc("UNREAD");
    }

    public EVVAlert markAsRead(Long id) {
        EVVAlert alert = evvAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EVV alert not found."));

        alert.setStatus("READ");
        alert.setReadAt(LocalDateTime.now());

        return evvAlertRepository.save(alert);
    }

    private String buildMessage(EVVException exception) {
        return exception.getExceptionType()
                + " detected for "
                + exception.getClient().getFullName()
                + " with caregiver "
                + exception.getCaregiver().getFullName()
                + ".";
    }
}