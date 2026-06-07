package com.homecare.repository;

import com.homecare.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId);

    List<AuditLog> findByActorUserIdInOrderByCreatedAtDesc(List<Long> actorUserIds);

    List<AuditLog> findByClientIdInOrderByCreatedAtDesc(List<Long> clientIds);

    List<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
            String resourceType,
            Long resourceId
    );

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}