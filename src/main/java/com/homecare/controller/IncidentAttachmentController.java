package com.homecare.controller;

import com.homecare.dto.IncidentAttachmentResponse;
import com.homecare.entity.IncidentAttachment;
import com.homecare.service.IncidentAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin("*")
public class IncidentAttachmentController {

    private final IncidentAttachmentService attachmentService;

    public IncidentAttachmentController(IncidentAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/{incidentId}/attachments")
    public IncidentAttachmentResponse uploadAttachment(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file
    ) {
        return attachmentService.uploadAttachment(incidentId, file);
    }

    @GetMapping("/{incidentId}/attachments")
    public List<IncidentAttachmentResponse> getAttachments(
            @PathVariable Long incidentId
    ) {
        return attachmentService.getAttachmentsByIncident(incidentId);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId
    ) {
        Resource resource = attachmentService.downloadAttachment(attachmentId);
        IncidentAttachment attachment = attachmentService.getAttachmentEntity(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getFileType() != null
                                ? attachment.getFileType()
                                : "application/octet-stream"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\""
                )
                .body(resource);
    }
}