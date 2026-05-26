package com.homecare.repository;

import com.homecare.entity.ISPPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISPPlanRepository extends JpaRepository<ISPPlan, Long> {

    List<ISPPlan> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<ISPPlan> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, String status);
}