package com.gadgetgo.booking.api.dto;

import java.util.List;

public record JobDetailResponse(
        BookingResponse booking,
        QuotationResponse currentQuotation,
        List<QuotationResponse> quotationHistory,
        List<RepairStatusHistoryResponse> statusHistory
) {}
