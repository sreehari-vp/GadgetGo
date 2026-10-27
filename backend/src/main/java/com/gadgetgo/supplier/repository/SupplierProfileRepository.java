package com.gadgetgo.supplier.repository;

import com.gadgetgo.supplier.domain.SupplierProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierProfileRepository extends JpaRepository<SupplierProfile, Long> {
    boolean existsByAccount_IdAndActiveTrue(Long accountId);

    @EntityGraph(attributePaths = "account")
    Optional<SupplierProfile> findByAccount_Id(Long accountId);
}
