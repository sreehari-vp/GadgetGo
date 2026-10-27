package com.gadgetgo.catalog.service;

import com.gadgetgo.catalog.api.dto.CategoryResponse;
import com.gadgetgo.catalog.api.dto.PageResponse;
import com.gadgetgo.catalog.api.dto.RepairTypeResponse;
import com.gadgetgo.catalog.api.dto.ServiceDetailResponse;
import com.gadgetgo.catalog.api.dto.ServiceSummaryResponse;
import com.gadgetgo.catalog.domain.DeviceCategory;
import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.domain.RepairType;
import com.gadgetgo.catalog.repository.DeviceCategoryRepository;
import com.gadgetgo.catalog.repository.RepairServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final DeviceCategoryRepository categoryRepository;
    private final RepairServiceRepository serviceRepository;

    public CatalogService(
            DeviceCategoryRepository categoryRepository,
            RepairServiceRepository serviceRepository) {
        this.categoryRepository = categoryRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<CategoryResponse> listActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(CatalogService::toCategoryResponse)
                .toList();
    }

    public List<RepairTypeResponse> listActiveRepairTypes() {
        return serviceRepository.findActiveRepairTypes().stream()
                .map(type -> new RepairTypeResponse(type.slug(), type.label()))
                .toList();
    }

    public PageResponse<ServiceSummaryResponse> listServices(
            String categorySlug,
            String searchQuery,
            String typeSlug,
            int page,
            int size,
            String sortOption) {
        RepairType type = parseRepairType(typeSlug);
        String normalizedQuery = normalizeQuery(searchQuery);

        Page<RepairService> servicePage = serviceRepository.findPublicServices(
                blankToNull(categorySlug),
                type == null ? null : type.name(),
                normalizedQuery,
                sortOption,
                PageRequest.of(page, size));

        Map<Long, DeviceCategory> categoriesById = new LinkedHashMap<>();
        categoryRepository.findAllById(servicePage.getContent().stream()
                        .map(service -> service.getCategory().getId())
                        .distinct()
                        .toList())
                .forEach(category -> categoriesById.put(category.getId(), category));

        Page<ServiceSummaryResponse> responsePage = servicePage.map(service ->
                toSummaryResponse(service, categoriesById.get(service.getCategory().getId())));
        return PageResponse.from(responsePage);
    }

    public ServiceDetailResponse getServiceBySlug(String slug) {
        RepairService service = serviceRepository
                .findBySlugAndActiveTrueAndCategory_ActiveTrue(slug)
                .orElseThrow(CatalogResourceNotFoundException::new);
        return toDetailResponse(service, service.getCategory());
    }

    private RepairType parseRepairType(String typeSlug) {
        if (typeSlug == null || typeSlug.isBlank()) {
            return null;
        }
        return RepairType.fromSlug(typeSlug.trim())
                .orElseThrow(() -> new InvalidCatalogFilterException("Unsupported repair type."));
    }

    private static String normalizeQuery(String query) {
        return blankToNull(query == null ? null : query.trim());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static CategoryResponse toCategoryResponse(DeviceCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconId());
    }

    private static ServiceSummaryResponse toSummaryResponse(
            RepairService service,
            DeviceCategory category) {
        RepairType type = service.getRepairType();
        return new ServiceSummaryResponse(
                service.getId(),
                service.getName(),
                service.getSlug(),
                service.getShortDescription(),
                toCategoryResponse(category),
                type.slug(),
                type.label());
    }

    private static ServiceDetailResponse toDetailResponse(
            RepairService service,
            DeviceCategory category) {
        RepairType type = service.getRepairType();
        return new ServiceDetailResponse(
                service.getId(),
                service.getName(),
                service.getSlug(),
                service.getShortDescription(),
                service.getDetailedDescription(),
                toCategoryResponse(category),
                type.slug(),
                type.label());
    }
}
