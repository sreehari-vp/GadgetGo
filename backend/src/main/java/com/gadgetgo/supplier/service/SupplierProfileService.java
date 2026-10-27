package com.gadgetgo.supplier.service;

import com.gadgetgo.security.ProviderProfileNotFoundException;
import com.gadgetgo.supplier.api.dto.SupplierProfileResponse;
import com.gadgetgo.supplier.repository.SupplierProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SupplierProfileService {

    private final SupplierProfileRepository profileRepository;

    public SupplierProfileService(SupplierProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public SupplierProfileResponse getProfile(Long accountId) {
        return profileRepository.findByAccount_Id(accountId)
                .map(profile -> new SupplierProfileResponse(profile.getId(), profile.getBusinessName(),
                        profile.getAccount().getFullName(), profile.getAccount().getEmail(), profile.getContactEmail(),
                        profile.isActive(), profile.getCreatedAt(), profile.getUpdatedAt()))
                .orElseThrow(ProviderProfileNotFoundException::new);
    }
}
