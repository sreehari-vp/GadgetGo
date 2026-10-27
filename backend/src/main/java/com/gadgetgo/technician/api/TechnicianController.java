package com.gadgetgo.technician.api;

import com.gadgetgo.security.CustomerPrincipal;
import com.gadgetgo.technician.api.dto.TechnicianProfileResponse;
import com.gadgetgo.technician.api.dto.TechnicianSkillResponse;
import com.gadgetgo.technician.service.TechnicianProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technician")
public class TechnicianController {

    private final TechnicianProfileService profileService;

    public TechnicianController(TechnicianProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public TechnicianProfileResponse profile(@AuthenticationPrincipal CustomerPrincipal principal) {
        return profileService.getProfile(principal.id());
    }

    @GetMapping("/skills")
    public List<TechnicianSkillResponse> skills(@AuthenticationPrincipal CustomerPrincipal principal) {
        return profileService.getSkills(principal.id());
    }
}
