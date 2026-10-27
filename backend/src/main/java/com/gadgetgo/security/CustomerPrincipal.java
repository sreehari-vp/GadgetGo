package com.gadgetgo.security;

import com.gadgetgo.customer.domain.CustomerAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record CustomerPrincipal(Long id, String fullName, String email, String passwordHash, String role, boolean enabled)
        implements UserDetails {

    public static CustomerPrincipal from(CustomerAccount customer, boolean enabled) {
        return new CustomerPrincipal(customer.getId(), customer.getFullName(), customer.getEmail(),
                customer.getPasswordHash(), customer.getRole().name(), enabled);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isEnabled() { return enabled; }
}
