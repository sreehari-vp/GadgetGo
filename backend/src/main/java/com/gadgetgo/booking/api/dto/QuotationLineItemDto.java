package com.gadgetgo.booking.api.dto;

import com.gadgetgo.booking.domain.QuotationLineItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record QuotationLineItemDto(
        Long id,
        @NotBlank(message = "Part name is required.")
        String partName,
        @Positive(message = "Quantity must be at least 1.")
        int quantity,
        @NotNull(message = "Unit price is required.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be non-negative.")
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
    public static QuotationLineItemDto from(QuotationLineItem item) {
        if (item == null) return null;
        return new QuotationLineItemDto(
                item.getId(),
                item.getPartName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
