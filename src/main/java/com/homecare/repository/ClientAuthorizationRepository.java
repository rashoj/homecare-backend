package com.homecare.repository;

import com.homecare.entity.ClientAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClientAuthorizationRepository extends JpaRepository<ClientAuthorization, Long> {

    List<ClientAuthorization> findByClientIdOrderByEndDateDesc(Long clientId);

    List<ClientAuthorization> findByStatus(String status);

    List<ClientAuthorization> findByEndDateBeforeAndStatus(LocalDate date, String status);

    List<ClientAuthorization> findByEndDateBetweenAndStatus(
            LocalDate start,
            LocalDate end,
            String status
    );
    List<ClientAuthorization> findByClientIdAndStatusOrderByEndDateDesc(
            Long clientId,
            String status
    );
}