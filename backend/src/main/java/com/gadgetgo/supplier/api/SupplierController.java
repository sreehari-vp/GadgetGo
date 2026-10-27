package com.gadgetgo.supplier.api;

import com.gadgetgo.security.CustomerPrincipal;
import com.gadgetgo.supplier.api.dto.SupplierProfileResponse;
import com.gadgetgo.supplier.service.SupplierProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    private final SupplierProfileService profileService;

    public SupplierController(SupplierProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public SupplierProfileResponse profile(@AuthenticationPrincipal CustomerPrincipal principal) {
        return profileService.getProfile(principal.id());
    }
}
