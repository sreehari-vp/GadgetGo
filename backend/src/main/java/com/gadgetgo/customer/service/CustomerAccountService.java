package com.gadgetgo.customer.service;

import com.gadgetgo.customer.api.dto.CustomerResponse;
import com.gadgetgo.customer.api.dto.RegisterRequest;
import com.gadgetgo.customer.api.dto.UpdateProfileRequest;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.security.CustomerPrincipal;
import com.gadgetgo.security.AccountEligibilityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class CustomerAccountService implements UserDetailsService {

    private final CustomerAccountRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountEligibilityService accountEligibilityService;

    public CustomerAccountService(CustomerAccountRepository customerRepository,
                                  PasswordEncoder passwordEncoder,
                                  AccountEligibilityService accountEligibilityService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountEligibilityService = accountEligibilityService;
    }

    @Transactional
    public CustomerResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String fullName = request.fullName().trim();
        String password = request.password();
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be 72 UTF-8 bytes or fewer.");
        }
        if (customerRepository.existsByEmail(email)) {
            throw new CustomerConflictException();
        }

        CustomerAccount customer = new CustomerAccount(fullName, email, passwordEncoder.encode(password));
        try {
            return CustomerResponse.from(customerRepository.saveAndFlush(customer));
        } catch (DataIntegrityViolationException exception) {
            // The database unique index remains authoritative during concurrent registrations.
            throw new CustomerConflictException();
        }
    }

    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long customerId) {
        return customerRepository.findById(customerId)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new UsernameNotFoundException("Customer account is unavailable."));
    }

    @Transactional
    public CustomerResponse updateProfile(Long customerId, UpdateProfileRequest request) {
        CustomerAccount customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer account is unavailable."));
        customer.updateFullName(request.fullName().trim());
        return CustomerResponse.from(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = normalizeEmail(username);
        return customerRepository.findByEmail(email)
                .map(account -> CustomerPrincipal.from(account, accountEligibilityService.isEligible(account)))
                .orElseThrow(() -> new UsernameNotFoundException("Email or password is incorrect."));
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
