package com.homecare.repository;

import com.homecare.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    boolean existsByClockRecordId(Long clockRecordId);

    Optional<Timesheet> findByClockRecordId(Long clockRecordId);

    List<Timesheet> findByClientIdOrderByClockInTimeDesc(Long clientId);

    List<Timesheet> findByCaregiverIdOrderByClockInTimeDesc(Long caregiverId);

    List<Timesheet> findByPayrollStatusOrderByClockInTimeDesc(String payrollStatus);

    List<Timesheet> findByBillingStatusOrderByClockInTimeDesc(String billingStatus);
}