package com.gadgetgo.booking.repository;

import com.gadgetgo.booking.domain.QuotationStatus;
import com.gadgetgo.booking.domain.RepairQuotation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepairQuotationRepository extends JpaRepository<RepairQuotation, Long> {

    @EntityGraph(attributePaths = {"lineItems", "technician", "technician.account", "booking"})
    List<RepairQuotation> findAllByBooking_IdOrderByVersionDesc(Long bookingId);

    @EntityGraph(attributePaths = {"lineItems", "technician", "technician.account", "booking"})
    Optional<RepairQuotation> findFirstByBooking_IdOrderByVersionDesc(Long bookingId);

    @EntityGraph(attributePaths = {"lineItems", "technician", "technician.account", "booking"})
    Optional<RepairQuotation> findFirstByBooking_IdAndStatusOrderByVersionDesc(Long bookingId, QuotationStatus status);

    @EntityGraph(attributePaths = {"lineItems", "technician", "technician.account", "booking"})
    Optional<RepairQuotation> findByIdAndTechnician_Account_Id(Long id, Long accountId);

    @EntityGraph(attributePaths = {"lineItems", "technician", "technician.account", "booking"})
    Optional<RepairQuotation> findByIdAndBooking_Customer_Id(Long id, Long customerId);
}
