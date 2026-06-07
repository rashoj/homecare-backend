package com.homecare.repository;

import com.homecare.entity.DemoRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {
    List<DemoRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}