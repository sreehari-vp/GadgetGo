package com.gadgetgo.gadget.api.dto;

import com.gadgetgo.catalog.api.dto.CategoryResponse;
import com.gadgetgo.catalog.domain.DeviceCategory;
import com.gadgetgo.gadget.domain.CustomerGadget;

import java.time.Instant;
import java.time.LocalDate;

public record GadgetResponse(
        Long id,
        CategoryResponse category,
        String brand,
        String model,
        LocalDate purchaseDate,
        LocalDate warrantyExpiryDate,
        Instant createdAt,
        Instant updatedAt) {

    public static GadgetResponse from(CustomerGadget gadget) {
        DeviceCategory category = gadget.getCategory();
        return new GadgetResponse(gadget.getId(),
                new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                        category.getDescription(), category.getIconId()),
                gadget.getBrand(), gadget.getModel(), gadget.getPurchaseDate(),
                gadget.getWarrantyExpiryDate(), gadget.getCreatedAt(), gadget.getUpdatedAt());
    }
}
