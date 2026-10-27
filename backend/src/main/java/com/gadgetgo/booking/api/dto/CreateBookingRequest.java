package com.gadgetgo.booking.api.dto;

import com.gadgetgo.technician.domain.TechnicianServiceMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        @NotNull @Positive Long gadgetId,
        @NotNull @Positive Long serviceId,
        @NotNull TechnicianServiceMode serviceMode,
        @NotBlank @Size(min = 10, max = 2000, message = "Problem description must be between 10 and 2000 characters.") String problemDescription
) { }
