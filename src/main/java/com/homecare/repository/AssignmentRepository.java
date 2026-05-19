package com.homecare.repository;

import com.homecare.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    Optional<AssignmentEntity> findFirstByCaregiverIdAndAssignmentDate(
            Long caregiverId,
            LocalDate assignmentDate
    );
}