package com.gadgetgo.customer.api;

import com.gadgetgo.customer.api.dto.CustomerResponse;
import com.gadgetgo.customer.api.dto.UpdateProfileRequest;
import com.gadgetgo.customer.service.CustomerAccountService;
import com.gadgetgo.security.CustomerPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/me")
public class CustomerController {

    private final CustomerAccountService customerService;

    public CustomerController(CustomerAccountService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public CustomerResponse profile(@AuthenticationPrincipal CustomerPrincipal principal) {
        return customerService.getProfile(principal.id());
    }

    @PutMapping
    public CustomerResponse updateProfile(@AuthenticationPrincipal CustomerPrincipal principal,
                                          @Valid @RequestBody UpdateProfileRequest request) {
        return customerService.updateProfile(principal.id(), request);
    }
}
