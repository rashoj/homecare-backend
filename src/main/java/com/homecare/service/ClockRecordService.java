package com.homecare.service;

import com.homecare.dto.ClockInRequest;
import com.homecare.dto.ClockOutRequest;
import com.homecare.dto.ClockRecordResponse;
import com.homecare.entity.Appointment;
import com.homecare.entity.ClockRecord;
import com.homecare.repository.AppointmentRepository;
import com.homecare.repository.ClockRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClockRecordService {

    private final ClockRecordRepository clockRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public ClockRecordService(ClockRecordRepository clockRecordRepository,
                              AppointmentRepository appointmentRepository) {
        this.clockRecordRepository = clockRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public ClockRecordResponse clockIn(ClockInRequest request) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (clockRecordRepository.existsByAppointmentId(request.getAppointmentId())) {
            throw new RuntimeException("Already clocked in for this appointment");
        }

        ClockRecord clockRecord = ClockRecord.builder()
                .appointment(appointment)
                .clockInTime(LocalDateTime.now())
                .clockInLatitude(request.getLatitude())
                .clockInLongitude(request.getLongitude())
                .clockInNotes(request.getNotes())
                .status("CLOCKED_IN")
                .build();

        appointment.setStatus("IN_PROGRESS");
        appointment.setCompleted(false);
        appointmentRepository.save(appointment);

        return mapToResponse(clockRecordRepository.save(clockRecord));
    }

    public ClockRecordResponse clockOut(ClockOutRequest request) {

        ClockRecord clockRecord = clockRecordRepository.findByAppointmentId(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Clock-in record not found"));

        if (clockRecord.getClockOutTime() != null) {
            throw new RuntimeException("Already clocked out for this appointment");
        }

        clockRecord.setClockOutTime(LocalDateTime.now());
        clockRecord.setClockOutLatitude(request.getLatitude());
        clockRecord.setClockOutLongitude(request.getLongitude());
        clockRecord.setClockOutNotes(request.getNotes());
        clockRecord.setStatus("CLOCKED_OUT");

        long minutes = Duration.between(clockRecord.getClockInTime(), clockRecord.getClockOutTime()).toMinutes();
        clockRecord.setTotalHours(minutes / 60.0);

        Appointment appointment = clockRecord.getAppointment();
        appointment.setStatus("COMPLETED");
        appointment.setCompleted(true);
        appointmentRepository.save(appointment);

        return mapToResponse(clockRecordRepository.save(clockRecord));
    }

    public List<ClockRecordResponse> getAllClockRecords() {
        return clockRecordRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClockRecordResponse getClockRecordByAppointment(Long appointmentId) {
        ClockRecord clockRecord = clockRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Clock record not found"));

        return mapToResponse(clockRecord);
    }

    private ClockRecordResponse mapToResponse(ClockRecord clockRecord) {

        Appointment appointment = clockRecord.getAppointment();

        return ClockRecordResponse.builder()
                .id(clockRecord.getId())
                .appointmentId(appointment.getId())
                .clientName(appointment.getClient().getFullName())
                .caregiverName(appointment.getCaregiver().getFullName())
                .clockInTime(clockRecord.getClockInTime())
                .clockOutTime(clockRecord.getClockOutTime())
                .clockInLatitude(clockRecord.getClockInLatitude())
                .clockInLongitude(clockRecord.getClockInLongitude())
                .clockOutLatitude(clockRecord.getClockOutLatitude())
                .clockOutLongitude(clockRecord.getClockOutLongitude())
                .totalHours(clockRecord.getTotalHours())
                .status(clockRecord.getStatus())
                .clockInNotes(clockRecord.getClockInNotes())
                .clockOutNotes(clockRecord.getClockOutNotes())
                .build();
    }
    public List<ClockRecordResponse> getClockRecordsByClient(Long clientId) {
        return clockRecordRepository.findByAppointmentClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}