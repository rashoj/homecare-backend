package com.homecare.controller;

import com.homecare.dto.ClockInRequest;
import com.homecare.dto.ClockOutRequest;
import com.homecare.dto.ClockRecordResponse;
import com.homecare.service.ClockRecordService;
import org.springframework.web.bind.annotation.*;
import com.homecare.dto.ClockRecordAdjustmentRequest;

import java.util.List;

@RestController
@RequestMapping("/api/clock")
@CrossOrigin("*")
public class ClockRecordController {

    private final ClockRecordService clockRecordService;

    public ClockRecordController(ClockRecordService clockRecordService) {
        this.clockRecordService = clockRecordService;
    }

    @PostMapping("/in")
    public ClockRecordResponse clockIn(@RequestBody ClockInRequest request) {
        return clockRecordService.clockIn(request);
    }

    @PostMapping("/out")
    public ClockRecordResponse clockOut(@RequestBody ClockOutRequest request) {
        return clockRecordService.clockOut(request);
    }
    @GetMapping("/client/{clientId}")
    public List<ClockRecordResponse> getClockRecordsByClient(@PathVariable Long clientId) {
        return clockRecordService.getClockRecordsByClient(clientId);
    }

    @GetMapping
    public List<ClockRecordResponse> getAllClockRecords() {
        return clockRecordService.getAllClockRecords();
    }

    @GetMapping("/appointment/{appointmentId}")
    public ClockRecordResponse getClockRecordByAppointment(@PathVariable Long appointmentId) {
        return clockRecordService.getClockRecordByAppointment(appointmentId);





    }
    @PutMapping("/{id}/admin-adjust")
    public ClockRecordResponse adminAdjustClockRecord(
            @PathVariable Long id,
            @RequestBody ClockRecordAdjustmentRequest request
    ) {
        return clockRecordService.adminAdjustClockRecord(id, request);
    }
}