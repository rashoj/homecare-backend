package com.homecare.repository;

import com.homecare.entity.ISPGoalProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISPGoalProgressLogRepository extends JpaRepository<ISPGoalProgressLog, Long> {

    List<ISPGoalProgressLog> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<ISPGoalProgressLog> findByServiceDocumentationIdOrderByCreatedAtDesc(Long serviceDocumentationId);

    List<ISPGoalProgressLog> findByGoalIdOrderByCreatedAtDesc(Long goalId);
}