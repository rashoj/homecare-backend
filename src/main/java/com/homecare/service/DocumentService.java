package com.homecare.service;

import com.homecare.dto.DocumentResponse;
import com.homecare.entity.Client;
import com.homecare.entity.Document;
import com.homecare.entity.User;
import com.homecare.repository.ClientRepository;
import com.homecare.repository.DocumentRepository;
import com.homecare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

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

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(DocumentRepository documentRepository,
                           UserRepository userRepository,
                           ClientRepository clientRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
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

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path filePath = uploadPath.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath);

            Document document = Document.builder()
                    .documentName(documentName)
                    .documentType(documentType)
                    .fileName(originalFileName)
                    .filePath(filePath.toString())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .expirationDate(expirationDate)
                    .approvalStatus("PENDING")
                    .uploadedBy(uploadedBy)
                    .client(client)
                    .build();

            return mapToResponse(documentRepository.save(document));

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

    public DocumentResponse approveDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setApprovalStatus("APPROVED");
        document.setRejectionReason(null);

        return mapToResponse(documentRepository.save(document));
    }

    public DocumentResponse rejectDocument(Long id, String reason) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setApprovalStatus("REJECTED");
        document.setRejectionReason(reason);

        return mapToResponse(documentRepository.save(document));
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
                .uploadedByUserId(document.getUploadedBy() != null ? document.getUploadedBy().getId() : null)
                .uploadedByName(document.getUploadedBy() != null ? document.getUploadedBy().getFullName() : null)
                .clientId(document.getClient() != null ? document.getClient().getId() : null)
                .clientName(document.getClient() != null ? document.getClient().getFullName() : null)
                .uploadedAt(document.getUploadedAt())
                .build();
    }
    public Document getDocumentEntity(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
    public Resource downloadDocument(Long documentId) {

        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            Path filePath = Paths.get(document.getFilePath()).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File not found on server");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("File download failed");
        }
    }
}