package com.gadgetgo.booking.api.dto;

import com.gadgetgo.booking.domain.RepairQuotation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record QuotationResponse(
        Long id,
        Long bookingId,
        Long technicianId,
        String technicianName,
        int version,
        String status,
        String diagnosedFault,
        String proposedRepair,
        String estimatedDuration,
        BigDecimal labourCharge,
        BigDecimal additionalCharge,
        BigDecimal partsTotal,
        BigDecimal totalAmount,
        String customerNotes,
        List<QuotationLineItemDto> lineItems,
        Instant createdAt,
        Instant submittedAt,
        Instant respondedAt,
        Instant updatedAt
) {
    public static QuotationResponse from(RepairQuotation q) {
        if (q == null) return null;
        String techName = q.getTechnician() != null && q.getTechnician().getAccount() != null
                ? q.getTechnician().getAccount().getFullName()
                : "Assigned Technician";

        List<QuotationLineItemDto> items = q.getLineItems() != null
                ? q.getLineItems().stream().map(QuotationLineItemDto::from).toList()
                : List.of();

        return new QuotationResponse(
                q.getId(),
                q.getBooking().getId(),
                q.getTechnician().getId(),
                techName,
                q.getVersion(),
                q.getStatus().name(),
                q.getDiagnosedFault(),
                q.getProposedRepair(),
                q.getEstimatedDuration(),
                q.getLabourCharge(),
                q.getAdditionalCharge(),
                q.getPartsTotal(),
                q.getTotalAmount(),
                q.getCustomerNotes(),
                items,
                q.getCreatedAt(),
                q.getSubmittedAt(),
                q.getRespondedAt(),
                q.getUpdatedAt()
        );
    }
}
