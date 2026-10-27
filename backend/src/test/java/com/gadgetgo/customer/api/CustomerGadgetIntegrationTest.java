package com.gadgetgo.customer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.supplier.domain.SupplierProfile;
import com.gadgetgo.supplier.repository.SupplierProfileRepository;
import com.gadgetgo.support.TestDatabaseSafetyInitializer;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@Transactional
class CustomerGadgetIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired CustomerAccountRepository accountRepository;
    @Autowired TechnicianProfileRepository technicianRepository;
    @Autowired SupplierProfileRepository supplierRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void registrationNormalizesEmailHashesPasswordAndIgnoresPrivilegedFields() throws Exception {
        Csrf csrf = csrf(null);
        mvc.perform(post("/api/auth/register").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json")
                        .content("""
                                {"fullName":" Ada Lovelace ","email":" ADA@example.com ","password":"a-long-safe-password","role":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.fullName").value("Ada Lovelace"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        String hash = jdbc.queryForObject("SELECT password_hash FROM customer_accounts WHERE email = ?", String.class, "ada@example.com");
        assertNotEquals("a-long-safe-password", hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void registrationRequiresCsrfValidatesFieldsAndRejectsDuplicateEmail() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"fullName\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"long-enough-password\"}"))
                .andExpect(status().isForbidden());
        Csrf csrf = csrf(null);
        mvc.perform(post("/api/auth/register").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json").content("{\"fullName\":\"A\",\"email\":\"bad\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
        register("duplicate@example.com", "Password-12345");
        mvc.perform(post("/api/auth/register").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json")
                        .content("{\"fullName\":\"Other Person\",\"email\":\"DUPLICATE@example.com\",\"password\":\"Password-12345\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginProfileGadgetOwnershipCsrfAndLogoutWorkTogether() throws Exception {
        register("owner@example.com", "Owner-Password-123");
        register("other@example.com", "Other-Password-123");
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/catalog/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));

        Csrf preAuthCsrf = csrf(null);
        MvcResult badLogin = mvc.perform(post("/api/auth/login").cookie(preAuthCsrf.cookie()).header(preAuthCsrf.header(), preAuthCsrf.token())
                        .contentType("application/json").content("{\"email\":\"owner@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized()).andReturn();
        assertTrue(badLogin.getResponse().getContentAsString().contains("incorrect"));

        MvcResult login = mvc.perform(post("/api/auth/login").cookie(preAuthCsrf.cookie()).header(preAuthCsrf.header(), preAuthCsrf.token())
                        .contentType("application/json").content(mapper.writeValueAsString(Map.of("email", " OWNER@example.com ", "password", "Owner-Password-123"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("owner@example.com"))
                .andReturn();
        MockHttpSession ownerSession = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(ownerSession);
        mvc.perform(get("/api/auth/me").session(ownerSession)).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("CUSTOMER"));
        mvc.perform(get("/api/customers/me").session(ownerSession)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Customer"));

        Csrf ownerCsrf = csrf(ownerSession);
        mvc.perform(put("/api/customers/me").session(ownerSession).cookie(ownerCsrf.cookie()).header(ownerCsrf.header(), ownerCsrf.token())
                        .contentType("application/json").content("{\"fullName\":\"Updated Owner\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Updated Owner"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        long categoryId = jdbc.queryForObject("SELECT id FROM device_categories WHERE slug = ?", Long.class, "smartphones");
        mvc.perform(post("/api/gadgets").session(ownerSession).cookie(ownerCsrf.cookie()).header(ownerCsrf.header(), ownerCsrf.token())
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("categoryId", 999999, "brand", "Acme", "model", "Unknown Category"))))
                .andExpect(status().isBadRequest());
        jdbc.update("UPDATE device_categories SET active = false WHERE id = ?", categoryId);
        mvc.perform(post("/api/gadgets").session(ownerSession).cookie(ownerCsrf.cookie()).header(ownerCsrf.header(), ownerCsrf.token())
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("categoryId", categoryId, "brand", "Acme", "model", "Inactive Category"))))
                .andExpect(status().isBadRequest());
        jdbc.update("UPDATE device_categories SET active = true WHERE id = ?", categoryId);
        MvcResult created = mvc.perform(post("/api/gadgets").session(ownerSession).cookie(ownerCsrf.cookie()).header(ownerCsrf.header(), ownerCsrf.token())
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("categoryId", categoryId, "brand", "Acme", "model", "Phone X", "customerId", 987654))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.brand").value("Acme"))
                .andExpect(jsonPath("$.category.slug").value("smartphones"))
                .andReturn();
        long gadgetId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/api/gadgets").session(ownerSession)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(gadgetId));
        mvc.perform(put("/api/gadgets/" + gadgetId).session(ownerSession).cookie(ownerCsrf.cookie()).header(ownerCsrf.header(), ownerCsrf.token())
                        .contentType("application/json").content(mapper.writeValueAsString(Map.of("categoryId", categoryId, "brand", "Acme", "model", "Phone Y", "customerId", 123456))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.model").value("Phone Y"));

        Csrf otherCsrf = csrf(null);
        MvcResult otherLogin = mvc.perform(post("/api/auth/login").cookie(otherCsrf.cookie()).header(otherCsrf.header(), otherCsrf.token())
                        .contentType("application/json").content("{\"email\":\"other@example.com\",\"password\":\"Other-Password-123\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession otherSession = (MockHttpSession) otherLogin.getRequest().getSession(false);
        mvc.perform(get("/api/gadgets").session(otherSession)).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/gadgets/" + gadgetId).session(otherSession)).andExpect(status().isNotFound());
        Csrf otherAuthCsrf = csrf(otherSession);
        mvc.perform(delete("/api/gadgets/" + gadgetId).session(otherSession).cookie(otherAuthCsrf.cookie()).header(otherAuthCsrf.header(), otherAuthCsrf.token()))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/gadgets/" + gadgetId).session(ownerSession))
                .andExpect(status().isForbidden());
        Csrf finalOwnerCsrf = csrf(ownerSession);
        mvc.perform(delete("/api/gadgets/" + gadgetId).session(ownerSession).cookie(finalOwnerCsrf.cookie()).header(finalOwnerCsrf.header(), finalOwnerCsrf.token()))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/logout").session(ownerSession).cookie(finalOwnerCsrf.cookie()).header(finalOwnerCsrf.header(), finalOwnerCsrf.token()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").session(ownerSession)).andExpect(status().isUnauthorized());
    }

    @Test
    void corsOnlyAllowsConfiguredFrontendOrigin() throws Exception {
        mvc.perform(options("/api/auth/register").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        mvc.perform(options("/api/auth/register").header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void sharedAuthenticationEnforcesProviderRolesAndPreservesCustomerAccess() throws Exception {
        register("role-customer@example.com", "Customer-Password-123");
        createTechnician("tech-role@example.com", "Technician-Password-123", true);
        createSupplier("supplier-role@example.com", "Supplier-Password-123", true);

        Csrf csrf = csrf(null);
        MvcResult techLogin = mvc.perform(post("/api/auth/login/technician").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json").content("""
                                {"email":"tech-role@example.com","password":"Technician-Password-123"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("TECHNICIAN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
        MockHttpSession techSession = (MockHttpSession) techLogin.getRequest().getSession(false);
        mvc.perform(get("/api/technician/me").session(techSession)).andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Tech role"));
        mvc.perform(get("/api/supplier/me").session(techSession)).andExpect(status().isForbidden());
        mvc.perform(get("/api/gadgets").session(techSession)).andExpect(status().isForbidden());

        Csrf wrongPortalCsrf = csrf(null);
        mvc.perform(post("/api/auth/login/supplier").cookie(wrongPortalCsrf.cookie()).header(wrongPortalCsrf.header(), wrongPortalCsrf.token())
                        .contentType("application/json").content("""
                                {"email":"tech-role@example.com","password":"Technician-Password-123"}
                                """))
                .andExpect(status().isUnauthorized());

        Csrf supplierCsrf = csrf(null);
        MvcResult supplierLogin = mvc.perform(post("/api/auth/login/supplier").cookie(supplierCsrf.cookie()).header(supplierCsrf.header(), supplierCsrf.token())
                        .contentType("application/json").content("""
                                {"email":"supplier-role@example.com","password":"Supplier-Password-123"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("SUPPLIER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
        MockHttpSession supplierSession = (MockHttpSession) supplierLogin.getRequest().getSession(false);
        mvc.perform(get("/api/supplier/me").session(supplierSession)).andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Supplier role business"));
        mvc.perform(get("/api/technician/me").session(supplierSession)).andExpect(status().isForbidden());

        Csrf customerCsrf = csrf(null);
        MvcResult customerLogin = mvc.perform(post("/api/auth/login").cookie(customerCsrf.cookie()).header(customerCsrf.header(), customerCsrf.token())
                        .contentType("application/json").content("""
                                {"email":"role-customer@example.com","password":"Customer-Password-123"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("CUSTOMER")).andReturn();
        MockHttpSession customerSession = (MockHttpSession) customerLogin.getRequest().getSession(false);
        mvc.perform(get("/api/technician/me").session(customerSession)).andExpect(status().isForbidden());
        mvc.perform(get("/api/supplier/me").session(customerSession)).andExpect(status().isForbidden());
        mvc.perform(get("/api/customers/me").session(customerSession)).andExpect(status().isOk());

        Csrf logoutCsrf = csrf(techSession);
        mvc.perform(post("/api/auth/logout").session(techSession).cookie(logoutCsrf.cookie()).header(logoutCsrf.header(), logoutCsrf.token()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/technician/me").session(techSession)).andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveProvidersCannotLoginOrContinueUsingAnExistingSession() throws Exception {
        TechnicianProfile inactive = createTechnician("inactive-tech@example.com", "Inactive-Password-123", false);
        createSupplier("inactive-supplier@example.com", "Inactive-Password-123", false);
        Csrf csrf = csrf(null);
        mvc.perform(post("/api/auth/login/technician").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json").content("""
                                {"email":"inactive-tech@example.com","password":"Inactive-Password-123"}
                                """))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login/supplier").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json").content("""
                                {"email":"inactive-supplier@example.com","password":"Inactive-Password-123"}
                                """))
                .andExpect(status().isUnauthorized());

        jdbc.update("UPDATE technician_profiles SET active = true WHERE id = ?", inactive.getId());
        Csrf activeCsrf = csrf(null);
        MvcResult loggedIn = mvc.perform(post("/api/auth/login/technician").cookie(activeCsrf.cookie()).header(activeCsrf.header(), activeCsrf.token())
                        .contentType("application/json").content("""
                                {"email":"inactive-tech@example.com","password":"Inactive-Password-123"}
                                """))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) loggedIn.getRequest().getSession(false);
        jdbc.update("UPDATE technician_profiles SET active = false WHERE id = ?", inactive.getId());
        mvc.perform(get("/api/technician/me").session(session)).andExpect(status().isUnauthorized());
    }

    private TechnicianProfile createTechnician(String email, String password, boolean active) {
        CustomerAccount account = accountRepository.saveAndFlush(new CustomerAccount("Tech role", email,
                passwordEncoder.encode(password), CustomerRole.TECHNICIAN));
        return technicianRepository.saveAndFlush(new TechnicianProfile(account, active, "North district",
                TechnicianAvailability.NOT_CONFIGURED, 3));
    }

    private SupplierProfile createSupplier(String email, String password, boolean active) {
        CustomerAccount account = accountRepository.saveAndFlush(new CustomerAccount("Supplier role", email,
                passwordEncoder.encode(password), CustomerRole.SUPPLIER));
        return supplierRepository.saveAndFlush(new SupplierProfile(account, "Supplier role business", null, active));
    }

    private void register(String email, String password) throws Exception {
        Csrf csrf = csrf(null);
        mvc.perform(post("/api/auth/register").cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("fullName", "Customer", "email", email, "password", password))))
                .andExpect(status().isCreated());
    }

    private Csrf csrf(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        MvcResult result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var json = mapper.readTree(result.getResponse().getContentAsString());
        String header = json.get("headerName").asText();
        String token = json.get("token").asText();
        String cookieName = "XSRF-TOKEN";
        Cookie cookie = result.getResponse().getCookie(cookieName);
        if (cookie == null) cookie = new Cookie(cookieName, token);
        return new Csrf(header, token, cookie);
    }

    private record Csrf(String header, String token, Cookie cookie) { }
}
