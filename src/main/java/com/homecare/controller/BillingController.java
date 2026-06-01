package com.homecare.controller;

import com.homecare.dto.BillingRecordRequest;
import com.homecare.dto.BillingRecordResponse;
import com.homecare.service.BillingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing-records")
@CrossOrigin("*")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/from-timesheet")
    public BillingRecordResponse createFromTimesheet(
            @RequestBody BillingRecordRequest request
    ) {
        return billingService.createFromTimesheet(request);
    }

    @GetMapping
    public List<BillingRecordResponse> getAllBillingRecords() {
        return billingService.getAllBillingRecords();
    }

    @GetMapping("/client/{clientId}")
    public List<BillingRecordResponse> getBillingRecordsByClient(
            @PathVariable Long clientId
    ) {
        return billingService.getBillingRecordsByClient(clientId);
    }

    @GetMapping("/status/{status}")
    public List<BillingRecordResponse> getBillingRecordsByStatus(
            @PathVariable String status
    ) {
        return billingService.getBillingRecordsByStatus(status);
    }

    @PutMapping("/{id}")
    public BillingRecordResponse updateBillingRecord(
            @PathVariable Long id,
            @RequestBody BillingRecordRequest request
    ) {
        return billingService.updateBillingRecord(id, request);
    }

    @PutMapping("/{id}/submit-claim")
    public BillingRecordResponse submitClaim(
            @PathVariable Long id,
            @RequestBody BillingRecordRequest request
    ) {
        return billingService.submitClaim(id, request);
    }

    @PutMapping("/{id}/paid")
    public BillingRecordResponse markPaid(
            @PathVariable Long id,
            @RequestBody BillingRecordRequest request
    ) {
        return billingService.markPaid(id, request);
    }

    @PutMapping("/{id}/deny")
    public BillingRecordResponse denyClaim(
            @PathVariable Long id,
            @RequestBody BillingRecordRequest request
    ) {
        return billingService.denyClaim(id, request);
    }

    @PutMapping("/{id}/void")
    public BillingRecordResponse voidBillingRecord(
            @PathVariable Long id,
            @RequestBody BillingRecordRequest request
    ) {
        return billingService.voidBillingRecord(id, request);
    }
}