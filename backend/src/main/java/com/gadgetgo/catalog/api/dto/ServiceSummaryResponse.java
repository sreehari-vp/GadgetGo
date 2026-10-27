package com.gadgetgo.catalog.api.dto;

public record ServiceSummaryResponse(
        Long id,
        String name,
        String slug,
        String shortDescription,
        CategoryResponse category,
        String repairType,
        String repairTypeLabel) {
}
