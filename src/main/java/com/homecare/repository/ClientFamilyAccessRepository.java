package com.homecare.repository;

import com.homecare.entity.ClientFamilyAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientFamilyAccessRepository extends JpaRepository<ClientFamilyAccess, Long> {

    Optional<ClientFamilyAccess> findByFamilyUserIdAndActiveTrue(Long familyUserId);
}