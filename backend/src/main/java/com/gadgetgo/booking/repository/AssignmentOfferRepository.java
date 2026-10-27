package com.gadgetgo.booking.repository;

import com.gadgetgo.booking.domain.AssignmentOffer;
import com.gadgetgo.booking.domain.OfferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentOfferRepository extends JpaRepository<AssignmentOffer, Long> {

    @EntityGraph(attributePaths = {"booking", "booking.repairService", "booking.repairService.category", "booking.gadget", "booking.gadget.category", "technician", "technician.account"})
    List<AssignmentOffer> findAllByTechnician_Account_IdAndStatusOrderByCreatedAtDesc(Long accountId, OfferStatus status);

    @EntityGraph(attributePaths = {"booking", "booking.repairService", "booking.gadget", "technician", "technician.account"})
    Optional<AssignmentOffer> findByIdAndTechnician_Account_Id(Long id, Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM AssignmentOffer o WHERE o.id = :id AND o.technician.account.id = :accountId")
    Optional<AssignmentOffer> findWithLockByIdAndTechnician_Account_Id(@Param("id") Long id, @Param("accountId") Long accountId);

    Optional<AssignmentOffer> findByBooking_IdAndStatus(Long bookingId, OfferStatus status);

    List<AssignmentOffer> findAllByBooking_Id(Long bookingId);

    boolean existsByBooking_IdAndTechnician_Id(Long bookingId, Long technicianProfileId);

    @Query("SELECT o.technician.id FROM AssignmentOffer o WHERE o.booking.id = :bookingId")
    List<Long> findOfferedTechnicianProfileIdsByBookingId(@Param("bookingId") Long bookingId);
}
