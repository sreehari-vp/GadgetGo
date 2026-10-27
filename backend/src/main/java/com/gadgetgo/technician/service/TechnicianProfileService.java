package com.gadgetgo.technician.service;

import com.gadgetgo.technician.api.dto.TechnicianProfileResponse;
import com.gadgetgo.technician.api.dto.TechnicianSkillResponse;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import com.gadgetgo.security.ProviderProfileNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TechnicianProfileService {

    private final TechnicianProfileRepository profileRepository;

    public TechnicianProfileService(TechnicianProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public TechnicianProfileResponse getProfile(Long accountId) {
        return profileRepository.findByAccount_Id(accountId)
                .map(TechnicianProfileService::toResponse)
                .orElseThrow(ProviderProfileNotFoundException::new);
    }

    public List<TechnicianSkillResponse> getSkills(Long accountId) {
        return getProfileEntity(accountId).getSkills().stream()
                .filter(service -> service.isActive() && service.getCategory().isActive())
                .map(service -> new TechnicianSkillResponse(service.getId(), service.getName(), service.getSlug(),
                        service.getCategory().getName(), service.getCategory().getSlug()))
                .sorted(Comparator.comparing(TechnicianSkillResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private TechnicianProfile getProfileEntity(Long accountId) {
        return profileRepository.findByAccount_Id(accountId).orElseThrow(ProviderProfileNotFoundException::new);
    }

    private static TechnicianProfileResponse toResponse(TechnicianProfile profile) {
        List<TechnicianSkillResponse> skills = profile.getSkills().stream()
                .filter(service -> service.isActive() && service.getCategory().isActive())
                .map(service -> new TechnicianSkillResponse(service.getId(), service.getName(), service.getSlug(),
                        service.getCategory().getName(), service.getCategory().getSlug()))
                .sorted(Comparator.comparing(TechnicianSkillResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<String> modes = profile.getServiceModes().stream().map(Enum::name).sorted().toList();
        return new TechnicianProfileResponse(profile.getId(), profile.getAccount().getFullName(),
                profile.getAccount().getEmail(), profile.isActive(), profile.getServiceArea(),
                profile.getAvailability().name(), profile.getCapacityLimit(), modes, skills,
                profile.getCreatedAt(), profile.getUpdatedAt());
    }
}
