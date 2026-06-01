package com.homecare.service;

import com.homecare.dto.DocumentResponse;
import com.homecare.entity.Client;
import com.homecare.entity.Document;
import com.homecare.entity.User;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.DocumentRepository;
import com.homecare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final AuditLogService auditLogService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            AuditLogService auditLogService
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.auditLogService = auditLogService;
    }

    public DocumentResponse uploadDocument(
            MultipartFile file,
            String documentName,
            String documentType,
            Long uploadedByUserId,
            Long clientId,
            LocalDate expirationDate
    ) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is required");
            }

            User uploadedBy = userRepository.findById(uploadedByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Client client = null;

            if (clientId != null) {
                client = clientRepository.findById(clientId)
                        .orElseThrow(() -> new RuntimeException("Client not found"));
            }

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String safeOriginalName = originalFileName != null
                    ? Paths.get(originalFileName).getFileName().toString()
                    : "uploaded-file";

            String storedFileName = UUID.randomUUID() + "_" + safeOriginalName;
            Path filePath = uploadPath.resolve(storedFileName).normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid file path.");
            }

            Files.copy(file.getInputStream(), filePath);

            Document document = Document.builder()
                    .documentName(documentName)
                    .documentType(documentType)
                    .fileName(safeOriginalName)
                    .filePath(filePath.toString())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .expirationDate(expirationDate)
                    .approvalStatus("PENDING")
                    .uploadedBy(uploadedBy)
                    .client(client)
                    .build();

            Document savedDocument = documentRepository.save(document);

            auditLogService.logAction(
                    uploadedBy.getId(),
                    uploadedBy.getFullName(),
                    uploadedBy.getRole().name(),
                    client != null ? client.getId() : null,
                    "UPLOAD_DOCUMENT",
                    "DOCUMENT",
                    savedDocument.getId(),
                    "Document uploaded."
            );

            return mapToResponse(savedDocument);

        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<DocumentResponse> getDocumentsByUser(Long userId) {
        return documentRepository.findByUploadedById(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<DocumentResponse> getDocumentsByClient(Long clientId) {
        return documentRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DocumentResponse approveDocument(Long id, Long actorUserId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User actor = getActor(actorUserId);

        document.setApprovalStatus("APPROVED");
        document.setRejectionReason(null);

        Document savedDocument = documentRepository.save(document);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedDocument.getClient() != null ? savedDocument.getClient().getId() : null,
                "APPROVE_DOCUMENT",
                "DOCUMENT",
                savedDocument.getId(),
                "Document approved."
        );

        return mapToResponse(savedDocument);
    }

    public DocumentResponse rejectDocument(Long id, String reason, Long actorUserId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User actor = getActor(actorUserId);

        document.setApprovalStatus("REJECTED");
        document.setRejectionReason(reason);

        Document savedDocument = documentRepository.save(document);

        auditLogService.logAction(
                actor.getId(),
                actor.getFullName(),
                actor.getRole().name(),
                savedDocument.getClient() != null ? savedDocument.getClient().getId() : null,
                "REJECT_DOCUMENT",
                "DOCUMENT",
                savedDocument.getId(),
                "Document rejected."
        );

        return mapToResponse(savedDocument);
    }

    public Document getDocumentEntity(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        auditLogService.logAction(
                document.getUploadedBy() != null ? document.getUploadedBy().getId() : null,
                document.getUploadedBy() != null ? document.getUploadedBy().getFullName() : "SYSTEM",
                document.getUploadedBy() != null ? document.getUploadedBy().getRole().name() : "SYSTEM",
                document.getClient() != null ? document.getClient().getId() : null,
                "VIEW_DOCUMENT_METADATA",
                "DOCUMENT",
                document.getId(),
                "Document metadata viewed."
        );

        return document;
    }

    public Resource downloadDocument(Long documentId, Long actorUserId) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            User actor = getActor(actorUserId);

            Path filePath = Paths.get(document.getFilePath()).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File not found on server");
            }

            auditLogService.logAction(
                    actor.getId(),
                    actor.getFullName(),
                    actor.getRole().name(),
                    document.getClient() != null ? document.getClient().getId() : null,
                    "DOWNLOAD_DOCUMENT",
                    "DOCUMENT",
                    document.getId(),
                    "Document downloaded."
            );

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("File download failed");
        }
    }

    private User getActor(Long actorUserId) {
        if (actorUserId == null) {
            throw new RuntimeException("Actor user is required for audit logging.");
        }

        return userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("Actor user not found."));
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .expirationDate(document.getExpirationDate())
                .approvalStatus(document.getApprovalStatus())
                .rejectionReason(document.getRejectionReason())
                .uploadedByUserId(
                        document.getUploadedBy() != null
                                ? document.getUploadedBy().getId()
                                : null
                )
                .uploadedByName(
                        document.getUploadedBy() != null
                                ? document.getUploadedBy().getFullName()
                                : null
                )
                .clientId(
                        document.getClient() != null
                                ? document.getClient().getId()
                                : null
                )
                .clientName(
                        document.getClient() != null
                                ? document.getClient().getFullName()
                                : null
                )
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}