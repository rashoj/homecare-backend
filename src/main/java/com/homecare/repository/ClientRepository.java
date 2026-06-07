package com.homecare.repository;

import com.homecare.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByActiveTrue();


    List<Client> findByOrganizationIdAndActiveTrue(Long organizationId);

    Optional<Client> findByIdAndOrganizationId(Long id, Long organizationId);
}