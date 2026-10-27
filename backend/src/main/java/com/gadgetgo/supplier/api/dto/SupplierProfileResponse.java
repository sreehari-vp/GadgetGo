package com.gadgetgo.supplier.api.dto;

import java.time.Instant;

public record SupplierProfileResponse(
        Long id,
        String businessName,
        String accountName,
        String accountEmail,
        String contactEmail,
        boolean active,
        Instant createdAt,
        Instant updatedAt) { }
