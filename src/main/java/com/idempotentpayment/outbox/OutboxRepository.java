package com.idempotentpayment.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Outbox Repository (DAO Layer).
 * 
 * Provides queries to fetch pending outbox events for background polling.
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, Long> {

    List<OutboxRecord> findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
