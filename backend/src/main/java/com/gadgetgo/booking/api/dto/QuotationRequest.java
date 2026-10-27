package com.gadgetgo.booking.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record QuotationRequest(
        @NotBlank(message = "Diagnosed fault description is required.")
        String diagnosedFault,

        @NotBlank(message = "Proposed repair description is required.")
        String proposedRepair,

        @NotBlank(message = "Estimated repair duration is required.")
        String estimatedDuration,

        @NotNull(message = "Labour charge is required.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Labour charge must be non-negative.")
        BigDecimal labourCharge,

        @DecimalMin(value = "0.0", inclusive = true, message = "Additional charge must be non-negative.")
        BigDecimal additionalCharge,

        @Valid
        List<QuotationLineItemDto> lineItems
) {}
