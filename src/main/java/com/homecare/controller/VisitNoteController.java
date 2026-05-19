package com.homecare.controller;

import com.homecare.dto.VisitNoteRequest;
import com.homecare.dto.VisitNoteResponse;
import com.homecare.service.VisitNoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visit-notes")
@CrossOrigin("*")
public class VisitNoteController {

    private final VisitNoteService visitNoteService;

    public VisitNoteController(VisitNoteService visitNoteService) {
        this.visitNoteService = visitNoteService;
    }

    @PostMapping
    public VisitNoteResponse createVisitNote(@RequestBody VisitNoteRequest request) {
        return visitNoteService.createVisitNote(request);
    }

    @PutMapping("/{id}")
    public VisitNoteResponse updateVisitNote(@PathVariable Long id,
                                             @RequestBody VisitNoteRequest request) {
        return visitNoteService.updateVisitNote(id, request);
    }

    @GetMapping
    public List<VisitNoteResponse> getAllVisitNotes() {
        return visitNoteService.getAllVisitNotes();
    }

    @GetMapping("/{id}")
    public VisitNoteResponse getVisitNoteById(@PathVariable Long id) {
        return visitNoteService.getVisitNoteById(id);
    }

    @GetMapping("/appointment/{appointmentId}")
    public VisitNoteResponse getVisitNoteByAppointment(@PathVariable Long appointmentId) {
        return visitNoteService.getVisitNoteByAppointment(appointmentId);
    }

    @GetMapping("/client/{clientId}")
    public List<VisitNoteResponse> getVisitNotesByClient(@PathVariable Long clientId) {
        return visitNoteService.getVisitNotesByClient(clientId);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<VisitNoteResponse> getVisitNotesByCaregiver(@PathVariable Long caregiverId) {
        return visitNoteService.getVisitNotesByCaregiver(caregiverId);
    }
    @PutMapping("/{id}/regenerate-summary")
    public VisitNoteResponse regenerateAiSummary(@PathVariable Long id) {
        return visitNoteService.regenerateAiSummary(id);
    }
}