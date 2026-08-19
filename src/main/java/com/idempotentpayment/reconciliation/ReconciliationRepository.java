package com.idempotentpayment.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Reconciliation Repository (DAO Layer).
 * 
 * Provides database queries for the reconciliation_errors table.
 */
@Repository
public interface ReconciliationRepository extends JpaRepository<ReconciliationError, Long> {

    List<ReconciliationError> findByReconciliationDate(LocalDate date);
}
