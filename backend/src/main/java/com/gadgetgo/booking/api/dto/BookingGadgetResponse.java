package com.gadgetgo.booking.api.dto;

import com.gadgetgo.gadget.domain.CustomerGadget;

public record BookingGadgetResponse(
        Long id,
        String brand,
        String model,
        String categoryName,
        String categorySlug
) {
    public static BookingGadgetResponse from(CustomerGadget gadget) {
        return new BookingGadgetResponse(
                gadget.getId(),
                gadget.getBrand(),
                gadget.getModel(),
                gadget.getCategory().getName(),
                gadget.getCategory().getSlug()
        );
    }
}
