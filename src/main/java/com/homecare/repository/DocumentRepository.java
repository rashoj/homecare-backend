package com.homecare.repository;

import com.homecare.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUploadedById(Long userId);

    List<Document> findByClientId(Long clientId);

    List<Document> findByApprovalStatus(String approvalStatus);
    long countByApprovalStatus(String approvalStatus);
}