package com.homecare.service;

import com.homecare.dto.IncidentAttachmentResponse;
import com.homecare.entity.Incident;
import com.homecare.entity.IncidentAttachment;
import com.homecare.repository.IncidentAttachmentRepository;
import com.homecare.repository.IncidentRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentAttachmentService {

    private final IncidentRepository incidentRepository;
    private final IncidentAttachmentRepository attachmentRepository;

    private final Path uploadPath = Paths.get("uploads/incidents");

    public IncidentAttachmentService(
            IncidentRepository incidentRepository,
            IncidentAttachmentRepository attachmentRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public IncidentAttachmentResponse uploadAttachment(
            Long incidentId,
            MultipartFile file
    ) {
        try {
            Incident incident = incidentRepository.findById(incidentId)
                    .orElseThrow(() -> new RuntimeException("Incident not found"));

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String safeFileName = UUID.randomUUID() + "-" + originalFileName;

            Path targetPath = uploadPath.resolve(safeFileName);

            Files.copy(file.getInputStream(), targetPath);

            IncidentAttachment attachment = IncidentAttachment.builder()
                    .incident(incident)
                    .fileName(originalFileName)
                    .fileType(file.getContentType())
                    .filePath(targetPath.toString())
                    .fileSize(file.getSize())
                    .build();

            return mapToResponse(attachmentRepository.save(attachment));

        } catch (Exception error) {
            throw new RuntimeException("Failed to upload incident attachment.");
        }
    }

    public List<IncidentAttachmentResponse> getAttachmentsByIncident(Long incidentId) {
        return attachmentRepository.findByIncidentIdOrderByUploadedAtDesc(incidentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Resource downloadAttachment(Long attachmentId) {
        try {
            IncidentAttachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found"));

            Path path = Paths.get(attachment.getFilePath());

            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File not found.");
            }

            return resource;

        } catch (Exception error) {
            throw new RuntimeException("Failed to download attachment.");
        }
    }

    public IncidentAttachment getAttachmentEntity(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    private IncidentAttachmentResponse mapToResponse(IncidentAttachment attachment) {
        return IncidentAttachmentResponse.builder()
                .id(attachment.getId())
                .incidentId(attachment.getIncident().getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}