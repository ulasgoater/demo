package com.idempotentpayment.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Account Repository (DAO Layer).
 * 
 * Interacts with PostgreSQL to query and persist Account entities.
 * JpaRepository provides basic CRUD methods (save, findById, findAll, deleteById).
 * Derived query methods like findByAccountNumber are automatically translated into SQL queries by Spring Data JPA.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
}
