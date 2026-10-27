package com.gadgetgo.technician.api.dto;

import java.time.Instant;
import java.util.List;

public record TechnicianProfileResponse(
        Long id,
        String displayName,
        String email,
        boolean active,
        String serviceArea,
        String availability,
        int capacityLimit,
        List<String> serviceModes,
        List<TechnicianSkillResponse> skills,
        Instant createdAt,
        Instant updatedAt) { }
