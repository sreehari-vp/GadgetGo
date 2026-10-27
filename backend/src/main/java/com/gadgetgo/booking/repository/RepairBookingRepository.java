package com.gadgetgo.booking.repository;

import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepairBookingRepository extends JpaRepository<RepairBooking, Long> {

    @EntityGraph(attributePaths = {"gadget", "gadget.category", "repairService", "repairService.category", "assignedTechnician", "assignedTechnician.account"})
    List<RepairBooking> findAllByCustomer_IdOrderByCreatedAtDescIdDesc(Long customerId);

    @EntityGraph(attributePaths = {"gadget", "gadget.category", "repairService", "repairService.category", "assignedTechnician", "assignedTechnician.account"})
    Optional<RepairBooking> findByIdAndCustomer_Id(Long id, Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM RepairBooking b WHERE b.id = :id")
    Optional<RepairBooking> findWithLockById(@Param("id") Long id);

    long countByAssignedTechnician_IdAndStatus(Long technicianProfileId, BookingStatus status);

    long countByAssignedTechnician_IdAndStatusIn(Long technicianProfileId, java.util.Collection<BookingStatus> statuses);

    @EntityGraph(attributePaths = {"gadget", "gadget.category", "repairService", "repairService.category", "customer"})
    List<RepairBooking> findAllByAssignedTechnician_Account_IdAndStatusOrderByCreatedAtDesc(Long accountId, BookingStatus status);

    @EntityGraph(attributePaths = {"gadget", "gadget.category", "repairService", "repairService.category", "customer"})
    List<RepairBooking> findAllByAssignedTechnician_Account_IdAndStatusInOrderByCreatedAtDesc(Long accountId, java.util.Collection<BookingStatus> statuses);

    @EntityGraph(attributePaths = {"gadget", "gadget.category", "repairService", "repairService.category", "customer"})
    Optional<RepairBooking> findByIdAndAssignedTechnician_Account_Id(Long id, Long accountId);
}
