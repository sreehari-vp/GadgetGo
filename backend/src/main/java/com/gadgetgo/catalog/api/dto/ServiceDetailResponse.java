package com.gadgetgo.catalog.api.dto;

public record ServiceDetailResponse(
        Long id,
        String name,
        String slug,
        String shortDescription,
        String detailedDescription,
        CategoryResponse category,
        String repairType,
        String repairTypeLabel) {
}
