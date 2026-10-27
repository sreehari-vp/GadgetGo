package com.gadgetgo.customer.repository;

import com.gadgetgo.customer.domain.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    Optional<CustomerAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}
