package com.gadgetgo.security;

import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.supplier.repository.SupplierProfileRepository;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountEligibilityService {

    private final CustomerAccountRepository accountRepository;
    private final TechnicianProfileRepository technicianRepository;
    private final SupplierProfileRepository supplierRepository;

    public AccountEligibilityService(CustomerAccountRepository accountRepository,
                                     TechnicianProfileRepository technicianRepository,
                                     SupplierProfileRepository supplierRepository) {
        this.accountRepository = accountRepository;
        this.technicianRepository = technicianRepository;
        this.supplierRepository = supplierRepository;
    }

    public boolean isEligible(CustomerAccount account) {
        return account.isActive() && profileIsActive(account.getId(), account.getRole());
    }

    public boolean isEligible(Long accountId, String role) {
        return accountRepository.findById(accountId)
                .filter(CustomerAccount::isActive)
                .filter(account -> account.getRole().name().equals(role))
                .map(account -> profileIsActive(accountId, account.getRole()))
                .orElse(false);
    }

    private boolean profileIsActive(Long accountId, CustomerRole role) {
        return switch (role) {
            case CUSTOMER, ADMIN -> true;
            case TECHNICIAN -> technicianRepository.existsByAccount_IdAndActiveTrue(accountId);
            case SUPPLIER -> supplierRepository.existsByAccount_IdAndActiveTrue(accountId);
        };
    }
}
