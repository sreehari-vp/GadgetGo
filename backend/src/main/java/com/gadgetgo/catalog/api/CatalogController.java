package com.gadgetgo.catalog.api;

import com.gadgetgo.catalog.api.dto.CategoryResponse;
import com.gadgetgo.catalog.api.dto.PageResponse;
import com.gadgetgo.catalog.api.dto.RepairTypeResponse;
import com.gadgetgo.catalog.api.dto.ServiceDetailResponse;
import com.gadgetgo.catalog.api.dto.ServiceSummaryResponse;
import com.gadgetgo.catalog.service.CatalogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Validated
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalogService.listActiveCategories();
    }

    @GetMapping("/repair-types")
    public List<RepairTypeResponse> repairTypes() {
        return catalogService.listActiveRepairTypes();
    }

    @GetMapping("/services")
    public PageResponse<ServiceSummaryResponse> services(
            @RequestParam(required = false)
            @Size(max = 80)
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Category must be a lowercase slug.")
            String category,
            @RequestParam(name = "q", required = false)
            @Size(max = 120, message = "Search text must be 120 characters or fewer.")
            String query,
            @RequestParam(name = "type", required = false)
            @Size(max = 60)
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Repair type must be a lowercase slug.")
            String type,
            @RequestParam(defaultValue = "0") @Min(0) @Max(1_000_000) int page,
            @RequestParam(defaultValue = "9") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "name")
            @Pattern(regexp = "name|name-desc|type|newest|oldest", message = "Unsupported sort option.")
            String sort) {
        return catalogService.listServices(category, query, type, page, size, sort);
    }

    @GetMapping("/services/{slug}")
    public ServiceDetailResponse serviceBySlug(@PathVariable @Size(max = 120) String slug) {
        return catalogService.getServiceBySlug(slug);
    }
}
