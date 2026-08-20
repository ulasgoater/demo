package com.idempotentpayment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Repository (DAO Layer).
 * 
 * Provides database access for TransactionRecord entities.
 * Also contains query methods used by the Reconciliation Engine to sum transactions for a given date.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {

    Optional<TransactionRecord> findByTransactionReference(String transactionReference);

    List<TransactionRecord> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionRecord t WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :start AND :end" )
    BigDecimal sumSuccessfulTransactionsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
