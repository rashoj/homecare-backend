package com.homecare.service;

import com.homecare.dto.ClientPayrollResponse;
import com.homecare.entity.Client;
import com.homecare.entity.ClockRecord;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.ClockRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientPayrollService {

    private final ClockRecordRepository clockRecordRepository;
    private final ClientRepository clientRepository;

    public ClientPayrollService(ClockRecordRepository clockRecordRepository,
                                ClientRepository clientRepository) {
        this.clockRecordRepository = clockRecordRepository;
        this.clientRepository = clientRepository;
    }

    public ClientPayrollResponse calculateClientPayroll(Long clientId, Double rate) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        List<ClockRecord> records =
                clockRecordRepository.findByAppointmentClientId(clientId);

        double totalHours = records.stream()
                .filter(record -> record.getTotalHours() != null)
                .mapToDouble(ClockRecord::getTotalHours)
                .sum();

        double amountDue = totalHours * rate;

        return ClientPayrollResponse.builder()
                .clientId(client.getId())
                .clientName(client.getFullName())
                .hourlyRate(rate)
                .totalHours(totalHours)
                .amountDue(amountDue)
                .build();
    }
}