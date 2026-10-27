package com.gadgetgo.gadget.repository;

import com.gadgetgo.gadget.domain.CustomerGadget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerGadgetRepository extends JpaRepository<CustomerGadget, Long> {
    List<CustomerGadget> findAllByCustomer_IdOrderByCreatedAtDescIdDesc(Long customerId);
    Optional<CustomerGadget> findByIdAndCustomer_Id(Long id, Long customerId);
}
