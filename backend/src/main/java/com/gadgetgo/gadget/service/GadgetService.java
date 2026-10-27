package com.gadgetgo.gadget.service;

import com.gadgetgo.catalog.domain.DeviceCategory;
import com.gadgetgo.catalog.repository.DeviceCategoryRepository;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.gadget.api.dto.GadgetRequest;
import com.gadgetgo.gadget.api.dto.GadgetResponse;
import com.gadgetgo.gadget.domain.CustomerGadget;
import com.gadgetgo.gadget.repository.CustomerGadgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GadgetService {

    private final CustomerGadgetRepository gadgetRepository;
    private final CustomerAccountRepository customerRepository;
    private final DeviceCategoryRepository categoryRepository;

    public GadgetService(CustomerGadgetRepository gadgetRepository,
                         CustomerAccountRepository customerRepository,
                         DeviceCategoryRepository categoryRepository) {
        this.gadgetRepository = gadgetRepository;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<GadgetResponse> list(Long customerId) {
        return gadgetRepository.findAllByCustomer_IdOrderByCreatedAtDescIdDesc(customerId).stream()
                .map(GadgetResponse::from).toList();
    }

    public GadgetResponse get(Long customerId, Long gadgetId) {
        return gadgetRepository.findByIdAndCustomer_Id(gadgetId, customerId)
                .map(GadgetResponse::from)
                .orElseThrow(GadgetNotFoundException::new);
    }

    @Transactional
    public GadgetResponse create(Long customerId, GadgetRequest request) {
        validateDates(request);
        CustomerAccount customer = customerRepository.findById(customerId)
                .orElseThrow(GadgetNotFoundException::new);
        DeviceCategory category = findActiveCategory(request.categoryId());
        CustomerGadget gadget = new CustomerGadget(customer, category, request.brand(), request.model(),
                request.purchaseDate(), request.warrantyExpiryDate());
        return GadgetResponse.from(gadgetRepository.save(gadget));
    }

    @Transactional
    public GadgetResponse update(Long customerId, Long gadgetId, GadgetRequest request) {
        validateDates(request);
        CustomerGadget gadget = gadgetRepository.findByIdAndCustomer_Id(gadgetId, customerId)
                .orElseThrow(GadgetNotFoundException::new);
        gadget.updateDetails(findActiveCategory(request.categoryId()), request.brand(), request.model(),
                request.purchaseDate(), request.warrantyExpiryDate());
        return GadgetResponse.from(gadget);
    }

    @Transactional
    public void delete(Long customerId, Long gadgetId) {
        CustomerGadget gadget = gadgetRepository.findByIdAndCustomer_Id(gadgetId, customerId)
                .orElseThrow(GadgetNotFoundException::new);
        gadgetRepository.delete(gadget);
    }

    private DeviceCategory findActiveCategory(Long categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new InvalidGadgetException("Select an active device category."));
    }

    private static void validateDates(GadgetRequest request) {
        LocalDate purchase = request.purchaseDate();
        LocalDate warranty = request.warrantyExpiryDate();
        if (purchase != null && purchase.isAfter(LocalDate.now())) {
            throw new InvalidGadgetException("Purchase date cannot be in the future.");
        }
        if (purchase != null && warranty != null && warranty.isBefore(purchase)) {
            throw new InvalidGadgetException("Warranty expiry must be on or after the purchase date.");
        }
    }
}
