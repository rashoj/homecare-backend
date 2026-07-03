package com.homecare.ai.insight;

import com.homecare.ai.dto.AppointmentInsightDTO;
import com.homecare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AppointmentInsightService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentInsightService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public AppointmentInsightDTO getTodayInsight(Long organizationId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return new AppointmentInsightDTO(
                appointmentRepository.countByOrganizationIdAndStartTimeBetween(organizationId, start, end),
                appointmentRepository.countByOrganizationIdAndStatusAndStartTimeBetween(organizationId, "SCHEDULED", start, end),
                appointmentRepository.countByOrganizationIdAndStatusAndStartTimeBetween(organizationId, "COMPLETED", start, end),
                appointmentRepository.countByOrganizationIdAndCaregiverIdIsNullAndStartTimeBetween(organizationId, start, end)
        );
    }
}