package com.gadgetgo.catalog.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.gadgetgo.support.TestDatabaseSafetyInitializer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@Transactional
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsActiveCategoriesFromTheDatabase() throws Exception {
        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("desktop-computers"))
                .andExpect(jsonPath("$[0].iconId").value("desktop"))
                .andExpect(jsonPath("$[5]").doesNotExist());
    }

    @Test
    void returnsOnlyActiveServicesInAPaginatedStableOrder() throws Exception {
        mockMvc.perform(get("/api/catalog/services")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].name").value("Desktop computer cooling inspection"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(24))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/catalog/services").param("page", "4").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void filtersByCategoryAndRepairType() throws Exception {
        mockMvc.perform(get("/api/catalog/services")
                        .param("category", "smartphones")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.content[0].category.slug").value("smartphones"));

        mockMvc.perform(get("/api/catalog/services")
                        .param("type", "battery-replacement")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].repairType").value("battery-replacement"));
    }

    @Test
    void searchesCaseInsensitivelyAndTreatsBlankSearchAsNoFilter() throws Exception {
        mockMvc.perform(get("/api/catalog/services").param("q", "SCREEN").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));

        mockMvc.perform(get("/api/catalog/services").param("q", "   ").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(24));
    }

    @Test
    void validatesPaginationSortAndRepairType() throws Exception {
        mockMvc.perform(get("/api/catalog/services").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        mockMvc.perform(get("/api/catalog/services").param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/catalog/services").param("sort", "name;drop"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/catalog/services").param("type", "unknown-type"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsServiceDetailsAndNotFoundForUnknownSlug() throws Exception {
        mockMvc.perform(get("/api/catalog/services/smartphone-screen-repair"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphone screen repair"))
                .andExpect(jsonPath("$.detailedDescription").isNotEmpty())
                .andExpect(jsonPath("$.category.slug").value("smartphones"))
                .andExpect(jsonPath("$.repairType").value("screen-repair"))
                .andExpect(jsonPath("$.createdAt").doesNotExist());

        mockMvc.perform(get("/api/catalog/services/not-a-real-service"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The requested repair service was not found."));
    }

    @Test
    void excludesInactiveServicesAndServicesInInactiveCategories() throws Exception {
        jdbcTemplate.update("UPDATE repair_services SET active = false WHERE slug = ?", "smartphone-screen-repair");

        mockMvc.perform(get("/api/catalog/services/smartphone-screen-repair"))
                .andExpect(status().isNotFound());

        jdbcTemplate.update("UPDATE device_categories SET active = false WHERE slug = ?", "smartphones");

        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'smartphones')]").isEmpty());

        mockMvc.perform(get("/api/catalog/services").param("category", "smartphones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/catalog/services/smartphone-battery-replacement"))
                .andExpect(status().isNotFound());
    }

    @Test
    void categoryDeletionIsRestrictedWhileServicesReferenceIt() {
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM device_categories WHERE slug = ?", "smartphones"));
    }

    @Test
    void existingApplicationHealthEndpointStillWorks() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("GadgetGo API"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
