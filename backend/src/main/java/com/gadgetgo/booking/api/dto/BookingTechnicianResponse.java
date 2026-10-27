package com.gadgetgo.booking.api.dto;

import com.gadgetgo.technician.domain.TechnicianProfile;

public record BookingTechnicianResponse(
        Long id,
        String displayName,
        String serviceArea
) {
    public static BookingTechnicianResponse from(TechnicianProfile technician) {
        if (technician == null) return null;
        String name = technician.getAccount() != null ? technician.getAccount().getFullName() : "Technician #" + technician.getId();
        return new BookingTechnicianResponse(technician.getId(), name, technician.getServiceArea());
    }
}
