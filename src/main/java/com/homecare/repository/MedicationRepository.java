package com.homecare.repository;

import com.homecare.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByClientIdAndActiveTrue(Long clientId);

    List<Medication> findByActiveTrue();

    List<Medication> findByClientId(Long clientId);
}