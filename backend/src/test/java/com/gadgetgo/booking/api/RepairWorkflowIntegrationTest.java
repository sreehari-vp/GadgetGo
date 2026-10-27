package com.gadgetgo.booking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gadgetgo.booking.api.dto.CustomerQuotationDecisionRequest;
import com.gadgetgo.booking.api.dto.QuotationLineItemDto;
import com.gadgetgo.booking.api.dto.QuotationRequest;
import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.QuotationStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.domain.RepairQuotation;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.booking.repository.RepairQuotationRepository;
import com.gadgetgo.booking.repository.RepairStatusHistoryRepository;
import com.gadgetgo.catalog.domain.DeviceCategory;
import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.repository.DeviceCategoryRepository;
import com.gadgetgo.catalog.repository.RepairServiceRepository;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.gadget.domain.CustomerGadget;
import com.gadgetgo.gadget.repository.CustomerGadgetRepository;
import com.gadgetgo.support.TestDatabaseSafetyInitializer;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.domain.TechnicianServiceMode;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@Transactional
class RepairWorkflowIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private CustomerAccountRepository accountRepository;
    @Autowired private CustomerGadgetRepository gadgetRepository;
    @Autowired private DeviceCategoryRepository categoryRepository;
    @Autowired private RepairServiceRepository serviceRepository;
    @Autowired private TechnicianProfileRepository technicianRepository;
    @Autowired private RepairBookingRepository bookingRepository;
    @Autowired private RepairQuotationRepository quotationRepository;
    @Autowired private RepairStatusHistoryRepository historyRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private DeviceCategory smartphoneCategory;
    private RepairService phoneScreenRepair;

    private CustomerAccount customer;
    private CustomerGadget gadget;
    private TechnicianProfile assignedTechnician;
    private TechnicianProfile otherTechnician;
    private CustomerAccount otherCustomer;

    @BeforeEach
    void setUp() {
        smartphoneCategory = categoryRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .filter(c -> c.getSlug().equals("smartphones")).findFirst().orElseThrow();
        phoneScreenRepair = serviceRepository.findBySlugAndActiveTrueAndCategory_ActiveTrue("smartphone-screen-repair")
                .orElseThrow();

        customer = createCustomer("alice.customer@example.com", "Password-123", "Alice Customer");
        gadget = gadgetRepository.saveAndFlush(new CustomerGadget(customer, smartphoneCategory, "Apple", "iPhone 15", null, null));

        assignedTechnician = createTechnician("bob.tech@example.com", "Password-123", "Bob Technician",
                true, TechnicianAvailability.AVAILABLE, 4, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));

        otherTechnician = createTechnician("charlie.tech@example.com", "Password-123", "Charlie Technician",
                true, TechnicianAvailability.AVAILABLE, 4, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));

        otherCustomer = createCustomer("eve.customer@example.com", "Password-123", "Eve Customer");
    }

    private RepairBooking createAssignedBooking() {
        RepairBooking booking = new RepairBooking(customer, gadget, phoneScreenRepair, TechnicianServiceMode.HOME_VISIT, "Display flickers and touch is dead");
        booking.markOffered();
        booking.markAssigned(assignedTechnician);
        return bookingRepository.saveAndFlush(booking);
    }

    @Test
    void assignedTechnicianCanDiagnoseAndSubmitQuotationWithBackendCalculatedTotals() throws Exception {
        RepairBooking booking = createAssignedBooking();

        MockHttpSession techSession = login("bob.tech@example.com", "Password-123", "TECHNICIAN");
        Csrf csrf = csrf(techSession);

        // 1. Technician gets job details
        mvc.perform(get("/api/technician/jobs/" + booking.getId()).session(techSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.booking.id").value(booking.getId()))
                .andExpect(jsonPath("$.booking.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.booking.problemDescription").value("Display flickers and touch is dead"))
                .andExpect(jsonPath("$.currentQuotation").doesNotExist());

        // 2. Technician saves a draft quotation
        QuotationRequest draftReq = new QuotationRequest(
                "Cracked digitizer layer and damaged AMOLED ribbon cable",
                "Replace OLED screen assembly and reseal water-resistant gasket",
                "2 business days",
                new BigDecimal("60.00"),
                new BigDecimal("10.00"),
                List.of(
                        new QuotationLineItemDto(null, "OEM OLED Display Assembly", 1, new BigDecimal("130.00"), null),
                        new QuotationLineItemDto(null, "Adhesive Frame Gasket", 2, new BigDecimal("7.50"), null)
                )
        );

        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/quotation/draft")
                        .session(techSession).cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(draftReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.partsTotal").value(145.00)) // 130 + 15
                .andExpect(jsonPath("$.labourCharge").value(60.00))
                .andExpect(jsonPath("$.additionalCharge").value(10.00))
                .andExpect(jsonPath("$.totalAmount").value(215.00)); // 145 + 60 + 10

        // Customer cannot see DRAFT quotation
        MockHttpSession custSession = login("alice.customer@example.com", "Password-123", "CUSTOMER");
        mvc.perform(get("/api/bookings/" + booking.getId() + "/detail").session(custSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentQuotation").doesNotExist());

        // 3. Technician submits quotation
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/quotation/submit")
                        .session(techSession).cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(draftReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.submittedAt").exists())
                .andExpect(jsonPath("$.totalAmount").value(215.00));

        // Booking status is now QUOTED
        RepairBooking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.QUOTED, updatedBooking.getStatus());

        // Customer can now see the submitted quotation
        mvc.perform(get("/api/bookings/" + booking.getId() + "/detail").session(custSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentQuotation.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.currentQuotation.totalAmount").value(215.00))
                .andExpect(jsonPath("$.currentQuotation.diagnosedFault").value("Cracked digitizer layer and damaged AMOLED ribbon cable"));
    }

    @Test
    void unassignedTechnicianCannotAccessOrModifyJob() throws Exception {
        RepairBooking booking = createAssignedBooking();

        // Charlie tries to access Bob's assigned job
        MockHttpSession charlieSession = login("charlie.tech@example.com", "Password-123", "TECHNICIAN");
        Csrf csrf = csrf(charlieSession);

        mvc.perform(get("/api/technician/jobs/" + booking.getId()).session(charlieSession))
                .andExpect(status().isForbidden());

        QuotationRequest req = new QuotationRequest(
                "Fault", "Fix", "1 day", BigDecimal.TEN, BigDecimal.ZERO, List.of()
        );

        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/quotation/draft")
                        .session(charlieSession).cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/start-repair")
                        .session(charlieSession).cookie(csrf.cookie()).header(csrf.header(), csrf.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCanOnlyAccessOwnQuotation() throws Exception {
        RepairBooking booking = createAssignedBooking();

        // Eve tries to access Alice's booking
        MockHttpSession eveSession = login("eve.customer@example.com", "Password-123", "CUSTOMER");
        Csrf csrf = csrf(eveSession);

        mvc.perform(get("/api/bookings/" + booking.getId() + "/detail").session(eveSession))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/bookings/" + booking.getId() + "/quotation/approve")
                        .session(eveSession).cookie(csrf.cookie()).header(csrf.header(), csrf.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void customerApprovalAuthorizesRepairAndTechnicianCanStart() throws Exception {
        RepairBooking booking = createAssignedBooking();

        MockHttpSession techSession = login("bob.tech@example.com", "Password-123", "TECHNICIAN");
        Csrf techCsrf = csrf(techSession);

        QuotationRequest req = new QuotationRequest(
                "Damaged port", "Replace charging flex", "1 day",
                new BigDecimal("40.00"), BigDecimal.ZERO,
                List.of(new QuotationLineItemDto(null, "Type-C Flex Cable", 1, new BigDecimal("25.00"), null))
        );

        // Submit quotation
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/quotation/submit")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // CRITICAL BUSINESS RULE: Repair CANNOT start before approval!
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/start-repair")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isConflict());

        // Customer approves quotation
        MockHttpSession custSession = login("alice.customer@example.com", "Password-123", "CUSTOMER");
        Csrf custCsrf = csrf(custSession);

        mvc.perform(post("/api/bookings/" + booking.getId() + "/quotation/approve")
                        .session(custSession).cookie(custCsrf.cookie()).header(custCsrf.header(), custCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.respondedAt").exists());

        // Booking status is now APPROVED
        assertEquals(BookingStatus.APPROVED, bookingRepository.findById(booking.getId()).orElseThrow().getStatus());

        // Now technician can start repair
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/start-repair")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.repairStartedAt").exists());

        // Duplicate start repair is rejected
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/start-repair")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isConflict());
    }

    @Test
    void customerRejectionPreventsRepairUntilRevisedQuotationApproved() throws Exception {
        RepairBooking booking = createAssignedBooking();

        MockHttpSession techSession = login("bob.tech@example.com", "Password-123", "TECHNICIAN");
        Csrf techCsrf = csrf(techSession);

        QuotationRequest initialReq = new QuotationRequest(
                "Cracked screen", "Original Screen Replacement", "1 day",
                new BigDecimal("90.00"), BigDecimal.ZERO,
                List.of(new QuotationLineItemDto(null, "OEM Screen", 1, new BigDecimal("200.00"), null))
        );

        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/quotation/submit")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(initialReq)))
                .andExpect(status().isOk());

        // Customer rejects quotation
        MockHttpSession custSession = login("alice.customer@example.com", "Password-123", "CUSTOMER");
        Csrf custCsrf = csrf(custSession);

        mvc.perform(post("/api/bookings/" + booking.getId() + "/quotation/reject")
                        .session(custSession).cookie(custCsrf.cookie()).header(custCsrf.header(), custCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CustomerQuotationDecisionRequest("Total cost is higher than expected"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.customerNotes").value("Total cost is higher than expected"));

        // Booking status reverted to ASSIGNED
        assertEquals(BookingStatus.ASSIGNED, bookingRepository.findById(booking.getId()).orElseThrow().getStatus());

        // CRITICAL BUSINESS RULE: Repair CANNOT start after rejection!
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/start-repair")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isConflict());

        // Technician submits REVISED quotation (v2) with aftermarket part
        QuotationRequest revisedReq = new QuotationRequest(
                "Cracked screen", "High-grade Compatible Screen Replacement", "1 day",
                new BigDecimal("60.00"), BigDecimal.ZERO,
                List.of(new QuotationLineItemDto(null, "Compatible Grade-A Screen", 1, new BigDecimal("95.00"), null))
        );

        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/quotation/submit")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(revisedReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.totalAmount").value(155.00));

        // Customer approves revised quotation v2
        mvc.perform(post("/api/bookings/" + booking.getId() + "/quotation/approve")
                        .session(custSession).cookie(custCsrf.cookie()).header(custCsrf.header(), custCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.version").value(2));

        // Now repair can begin!
        mvc.perform(post("/api/technician/jobs/" + booking.getId() + "/start-repair")
                        .session(techSession).cookie(techCsrf.cookie()).header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    private CustomerAccount createCustomer(String email, String password, String name) {
        return accountRepository.saveAndFlush(new CustomerAccount(
                name, email, passwordEncoder.encode(password), CustomerRole.CUSTOMER
        ));
    }

    private TechnicianProfile createTechnician(String email, String password, String name, boolean active,
                                                TechnicianAvailability availability, int capacityLimit,
                                                Set<TechnicianServiceMode> modes, Set<RepairService> skills) {
        CustomerAccount account = accountRepository.saveAndFlush(new CustomerAccount(
                name, email, passwordEncoder.encode(password), CustomerRole.TECHNICIAN
        ));
        TechnicianProfile profile = new TechnicianProfile(account, active, "District 1", availability, capacityLimit);
        profile.setServiceModes(modes);
        profile.setSkills(skills);
        return technicianRepository.saveAndFlush(profile);
    }

    private MockHttpSession login(String email, String password, String role) throws Exception {
        Csrf csrf = csrf(null);
        String path = role.equals("TECHNICIAN") ? "/api/auth/login/technician" : "/api/auth/login";
        MvcResult res = mvc.perform(post(path).cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) res.getRequest().getSession(false);
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
