package com.homecare.repository;

import com.homecare.entity.ServiceDocumentationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceDocumentationAuditLogRepository
        extends JpaRepository<ServiceDocumentationAuditLog, Long> {

    List<ServiceDocumentationAuditLog> findByDocumentationIdOrderByCreatedAtDesc(Long documentationId);
}