package com.homecare.repository;

import com.homecare.entity.BillingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {

    boolean existsByTimesheetId(Long timesheetId);

    Optional<BillingRecord> findByTimesheetId(Long timesheetId);

    List<BillingRecord> findByClientIdOrderByServiceDateDesc(Long clientId);

    List<BillingRecord> findByStatusOrderByServiceDateDesc(String status);
}