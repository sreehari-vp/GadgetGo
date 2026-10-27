package com.gadgetgo.booking.api.dto;

import com.gadgetgo.booking.domain.RepairStatusHistory;

import java.time.Instant;

public record RepairStatusHistoryResponse(
        Long id,
        Long bookingId,
        String fromStatus,
        String toStatus,
        String actorRole,
        String notes,
        Instant createdAt
) {
    public static RepairStatusHistoryResponse from(RepairStatusHistory h) {
        if (h == null) return null;
        return new RepairStatusHistoryResponse(
                h.getId(),
                h.getBooking().getId(),
                h.getFromStatus() != null ? h.getFromStatus().name() : null,
                h.getToStatus().name(),
                h.getActorRole(),
                h.getNotes(),
                h.getCreatedAt()
        );
    }
}
