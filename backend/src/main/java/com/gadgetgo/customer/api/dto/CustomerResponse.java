package com.gadgetgo.customer.api.dto;

import com.gadgetgo.customer.domain.CustomerAccount;

public record CustomerResponse(Long id, String fullName, String email, String role) {
    public static CustomerResponse from(CustomerAccount customer) {
        return new CustomerResponse(customer.getId(), customer.getFullName(), customer.getEmail(), customer.getRole().name());
    }
}
