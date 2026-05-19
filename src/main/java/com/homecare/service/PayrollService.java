package com.homecare.service;

import com.homecare.dto.PayrollResponse;
import com.homecare.entity.ClockRecord;
import com.homecare.entity.User;
import com.homecare.repository.ClockRecordRepository;
import com.homecare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayrollService {

    private final ClockRecordRepository clockRecordRepository;
    private final UserRepository userRepository;

    public PayrollService(ClockRecordRepository clockRecordRepository,
                          UserRepository userRepository) {
        this.clockRecordRepository = clockRecordRepository;
        this.userRepository = userRepository;
    }

    public PayrollResponse calculateCaregiverPayroll(Long caregiverId, Double hourlyRate) {

        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));

        List<ClockRecord> records =
                clockRecordRepository.findByAppointmentCaregiverId(caregiverId);

        double totalHours = records.stream()
                .filter(record -> record.getTotalHours() != null)
                .mapToDouble(ClockRecord::getTotalHours)
                .sum();

        double totalPay = totalHours * hourlyRate;

        return PayrollResponse.builder()
                .caregiverId(caregiver.getId())
                .caregiverName(caregiver.getFullName())
                .hourlyRate(hourlyRate)
                .totalHours(totalHours)
                .totalPay(totalPay)
                .build();
    }
}