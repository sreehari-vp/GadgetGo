package com.gadgetgo.catalog.api.dto;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String iconId) {
}
