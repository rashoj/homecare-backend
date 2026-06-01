package com.homecare.controller;

import com.homecare.dto.ClientPayrollResponse;
import com.homecare.service.ClientPayrollService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client-payroll")
@CrossOrigin("*")
public class ClientPayrollController {

    private final ClientPayrollService clientPayrollService;

    public ClientPayrollController(ClientPayrollService clientPayrollService) {
        this.clientPayrollService = clientPayrollService;
    }

    @GetMapping("/client/{clientId}")
    public ClientPayrollResponse calculateClientPayroll(
            @PathVariable Long clientId,
            @RequestParam Double rate,
            @RequestParam Long actorUserId
    ) {
        return clientPayrollService.calculateClientPayroll(
                clientId,
                rate,
                actorUserId
        );
    }
}