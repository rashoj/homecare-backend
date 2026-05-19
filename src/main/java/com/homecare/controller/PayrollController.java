package com.homecare.controller;

import com.homecare.dto.PayrollResponse;
import com.homecare.service.PayrollService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll")
@CrossOrigin("*")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/caregiver/{caregiverId}")
    public PayrollResponse calculateCaregiverPayroll(
            @PathVariable Long caregiverId,
            @RequestParam Double hourlyRate
    ) {
        return payrollService.calculateCaregiverPayroll(caregiverId, hourlyRate);
    }
}