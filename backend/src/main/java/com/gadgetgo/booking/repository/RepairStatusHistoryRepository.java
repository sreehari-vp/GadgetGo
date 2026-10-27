package com.gadgetgo.booking.repository;

import com.gadgetgo.booking.domain.RepairStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairStatusHistoryRepository extends JpaRepository<RepairStatusHistory, Long> {
    List<RepairStatusHistory> findAllByBooking_IdOrderByCreatedAtAsc(Long bookingId);
}
