package com.homecare.controller;

import com.homecare.dto.DocumentResponse;
import com.homecare.service.DocumentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.homecare.entity.Document;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin("*")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public DocumentResponse uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            @RequestParam("documentType") String documentType,
            @RequestParam("uploadedByUserId") Long uploadedByUserId,
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "expirationDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate
    ) {
        return documentService.uploadDocument(
                file,
                documentName,
                documentType,
                uploadedByUserId,
                clientId,
                expirationDate
        );
    }

    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/user/{userId}")
    public List<DocumentResponse> getDocumentsByUser(@PathVariable Long userId) {
        return documentService.getDocumentsByUser(userId);
    }

    @GetMapping("/client/{clientId}")
    public List<DocumentResponse> getDocumentsByClient(@PathVariable Long clientId) {
        return documentService.getDocumentsByClient(clientId);
    }

    @PutMapping("/{id}/approve")
    public DocumentResponse approveDocument(@PathVariable Long id) {
        return documentService.approveDocument(id);
    }

    @PutMapping("/{id}/reject")
    public DocumentResponse rejectDocument(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        return documentService.rejectDocument(id, reason);
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {

        Document document = documentService.getDocumentEntity(id);

        Resource resource = documentService.downloadDocument(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFileName() + "\""
                )
                .body(resource);
    }
}