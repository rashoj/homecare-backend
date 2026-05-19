package com.homecare.repository;

import com.homecare.entity.IncidentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentAttachmentRepository extends JpaRepository<IncidentAttachment, Long> {

    List<IncidentAttachment> findByIncidentIdOrderByUploadedAtDesc(Long incidentId);
}