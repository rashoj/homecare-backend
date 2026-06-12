package com.homecare.repository;

import com.homecare.entity.Role;
import com.homecare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<User> findByOrganizationIdAndRoleOrderByCreatedAtDesc(Long organizationId, Role role);


    boolean existsByEmail(String email);

    long countByRole(Role role);

    List<User> findByRole(Role role);

    long countByRoleName(String roleName);
}