package com.idempotentpayment.account;

import org.springframework.data.jpa.repository.JpaRepository;
//JPA stands for Java Persistence API. 
// It is the standard Java specification for mapping Java objects to database tables
//  and managing database persistence.
public interface AccountRepository extends JpaRepository<Account, Integer> {
    
}
