package com.homecare.repository;

import com.homecare.entity.IncidentReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentReviewRepository extends JpaRepository<IncidentReview, Long> {

    Optional<IncidentReview> findByIncidentId(Long incidentId);

    boolean existsByIncidentId(Long incidentId);
}