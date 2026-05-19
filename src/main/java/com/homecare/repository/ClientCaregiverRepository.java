package com.homecare.repository;

import com.homecare.entity.ClientCaregiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientCaregiverRepository extends JpaRepository<ClientCaregiver, Long> {

    List<ClientCaregiver> findByClientIdAndActiveTrue(Long clientId);

    Optional<ClientCaregiver> findByClientIdAndCaregiverIdAndActiveTrue(
            Long clientId,
            Long caregiverId
    );

    boolean existsByClientIdAndCaregiverIdAndActiveTrue(
            Long clientId,
            Long caregiverId
    );
}