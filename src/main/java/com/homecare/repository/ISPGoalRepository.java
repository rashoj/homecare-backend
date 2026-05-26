package com.homecare.repository;

import com.homecare.entity.ISPGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISPGoalRepository extends JpaRepository<ISPGoal, Long> {

    List<ISPGoal> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<ISPGoal> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, String status);
}