package com.gadgetgo.gadget.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GadgetRequest(
        @NotNull @Positive Long categoryId,
        @NotBlank @Size(max = 100) String brand,
        @NotBlank @Size(max = 160) String model,
        LocalDate purchaseDate,
        LocalDate warrantyExpiryDate) {
}
