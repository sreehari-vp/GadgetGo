package com.gadgetgo.booking.api.dto;

import com.gadgetgo.booking.domain.RepairBooking;

import java.time.Instant;

public record BookingResponse(
        Long id,
        String status,
        String serviceMode,
        String problemDescription,
        BookingGadgetResponse gadget,
        BookingServiceResponse service,
        BookingTechnicianResponse assignedTechnician,
        Instant repairStartedAt,
        Instant repairCompletedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static BookingResponse from(RepairBooking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getStatus().name(),
                booking.getServiceMode().name(),
                booking.getProblemDescription(),
                BookingGadgetResponse.from(booking.getGadget()),
                BookingServiceResponse.from(booking.getRepairService()),
                BookingTechnicianResponse.from(booking.getAssignedTechnician()),
                booking.getRepairStartedAt(),
                booking.getRepairCompletedAt(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
