package com.homecare.repository;

import com.homecare.entity.EVVExceptionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EVVExceptionAuditLogRepository extends JpaRepository<EVVExceptionAuditLog, Long> {

    List<EVVExceptionAuditLog> findByExceptionIdOrderByCreatedAtDesc(Long exceptionId);
}