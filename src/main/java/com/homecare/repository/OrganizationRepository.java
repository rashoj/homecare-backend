
package com.homecare.repository;

import com.homecare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}