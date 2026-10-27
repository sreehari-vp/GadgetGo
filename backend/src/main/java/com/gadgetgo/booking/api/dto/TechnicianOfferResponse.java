package com.gadgetgo.booking.api.dto;

import com.gadgetgo.booking.domain.AssignmentOffer;

import java.time.Instant;

public record TechnicianOfferResponse(
        Long id,
        Long bookingId,
        String status,
        BookingServiceResponse service,
        BookingGadgetResponse gadget,
        String serviceMode,
        String problemDescription,
        Double matchScore,
        String matchRationale,
        Instant createdAt,
        Instant respondedAt
) {
    public static TechnicianOfferResponse from(AssignmentOffer offer) {
        return new TechnicianOfferResponse(
                offer.getId(),
                offer.getBooking().getId(),
                offer.getStatus().name(),
                BookingServiceResponse.from(offer.getBooking().getRepairService()),
                BookingGadgetResponse.from(offer.getBooking().getGadget()),
                offer.getBooking().getServiceMode().name(),
                offer.getBooking().getProblemDescription(),
                offer.getMatchScore(),
                offer.getMatchRationale(),
                offer.getCreatedAt(),
                offer.getRespondedAt()
        );
    }
}
