package com.gadgetgo.booking.api.dto;

import com.gadgetgo.catalog.domain.RepairService;

public record BookingServiceResponse(
        Long id,
        String name,
        String slug,
        String categoryName,
        String categorySlug,
        String repairType
) {
    public static BookingServiceResponse from(RepairService service) {
        return new BookingServiceResponse(
                service.getId(),
                service.getName(),
                service.getSlug(),
                service.getCategory().getName(),
                service.getCategory().getSlug(),
                service.getRepairType().slug()
        );
    }
}
