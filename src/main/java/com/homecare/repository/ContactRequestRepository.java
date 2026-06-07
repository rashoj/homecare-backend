package com.homecare.repository;

import com.homecare.entity.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}