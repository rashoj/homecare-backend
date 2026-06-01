package com.homecare.service;

import com.homecare.dto.ClientPayrollResponse;
import com.homecare.entity.Client;
import com.homecare.entity.ClockRecord;
import com.homecare.entity.User;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.ClockRecordRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientPayrollService {

    private final ClockRecordRepository clockRecordRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ClientPayrollService(
            ClockRecordRepository clockRecordRepository,
            ClientRepository clientRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.clockRecordRepository = clockRecordRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public ClientPayrollResponse calculateClientPayroll(
            Long clientId,
            Double rate,
            Long actorUserId
    ) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("Actor user not found"));

        List<ClockRecord> records =
                clockRecordRepository.findByAppointmentClientId(clientId);

        double totalHours = records.stream()
                .filter(record -> record.getTotalHours() != null)
                .mapToDouble(ClockRecord::getTotalHours)
                .sum();

        double amountDue = totalHours * rate;

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                client.getId(),
                "CALCULATE_CLIENT_BILLING",
                "CLIENT_PAYROLL",
                client.getId(),
                "Client billing/payroll calculated."
        );

        return ClientPayrollResponse.builder()
                .clientId(client.getId())
                .clientName(client.getFullName())
                .hourlyRate(rate)
                .totalHours(totalHours)
                .amountDue(amountDue)
                .build();
    }
}