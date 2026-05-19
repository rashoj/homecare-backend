package com.homecare.controller;

import com.homecare.dto.ClientRiskRowResponse;
import com.homecare.service.RiskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin("*")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/clients")
    public List<ClientRiskRowResponse> getClientRiskRows() {
        return riskService.getClientRiskRows();
    }
}