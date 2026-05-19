package com.homecare.controller;

import com.homecare.dto.TimesheetResponse;
import com.homecare.dto.TimesheetReviewRequest;
import com.homecare.service.TimesheetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
@CrossOrigin("*")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @PostMapping("/generate/clock-record/{clockRecordId}")
    public TimesheetResponse generateFromClockRecord(
            @PathVariable Long clockRecordId
    ) {
        return timesheetService.generateFromClockRecord(clockRecordId);
    }

    @GetMapping
    public List<TimesheetResponse> getAllTimesheets() {
        return timesheetService.getAllTimesheets();
    }

    @GetMapping("/client/{clientId}")
    public List<TimesheetResponse> getTimesheetsByClient(
            @PathVariable Long clientId
    ) {
        return timesheetService.getTimesheetsByClient(clientId);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<TimesheetResponse> getTimesheetsByCaregiver(
            @PathVariable Long caregiverId
    ) {
        return timesheetService.getTimesheetsByCaregiver(caregiverId);
    }

    @PutMapping("/{id}/review")
    public TimesheetResponse reviewTimesheet(
            @PathVariable Long id,
            @RequestBody TimesheetReviewRequest request
    ) {
        return timesheetService.reviewTimesheet(id, request);
    }
}