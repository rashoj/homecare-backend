package com.homecare.controller;

import com.homecare.dto.*;
import com.homecare.service.MedicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@CrossOrigin("*")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    public MedicationResponse createMedication(@RequestBody MedicationRequest request) {
        return medicationService.createMedication(request);
    }

    @GetMapping
    public List<MedicationResponse> getAllMedications() {
        return medicationService.getAllMedications();
    }

    @GetMapping("/client/{clientId}")
    public List<MedicationResponse> getMedicationsByClient(@PathVariable Long clientId) {
        return medicationService.getMedicationsByClient(clientId);
    }

    @PutMapping("/{id}")
    public MedicationResponse updateMedication(@PathVariable Long id,
                                               @RequestBody MedicationRequest request) {
        return medicationService.updateMedication(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return "Medication deleted successfully";
    }

    @PostMapping("/logs")
    public MedicationLogResponse logMedication(@RequestBody MedicationLogRequest request) {
        return medicationService.logMedication(request);
    }

    @GetMapping("/logs/client/{clientId}")
    public List<MedicationLogResponse> getMedicationLogsByClient(@PathVariable Long clientId) {
        return medicationService.getMedicationLogsByClient(clientId);
    }

    @GetMapping("/logs/medication/{medicationId}")
    public List<MedicationLogResponse> getMedicationLogsByMedication(@PathVariable Long medicationId) {
        return medicationService.getMedicationLogsByMedication(medicationId);
    }
}