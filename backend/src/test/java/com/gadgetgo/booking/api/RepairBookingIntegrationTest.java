package com.gadgetgo.booking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gadgetgo.booking.api.dto.CreateBookingRequest;
import com.gadgetgo.booking.domain.AssignmentOffer;
import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.OfferStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.repository.AssignmentOfferRepository;
import com.gadgetgo.booking.repository.RepairBookingRepository;
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
class RepairBookingIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private CustomerAccountRepository accountRepository;
    @Autowired private CustomerGadgetRepository gadgetRepository;
    @Autowired private DeviceCategoryRepository categoryRepository;
    @Autowired private RepairServiceRepository serviceRepository;
    @Autowired private TechnicianProfileRepository technicianRepository;
    @Autowired private RepairBookingRepository bookingRepository;
    @Autowired private AssignmentOfferRepository offerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private DeviceCategory smartphoneCategory;
    private DeviceCategory laptopCategory;
    private RepairService phoneScreenRepair;
    private RepairService laptopScreenRepair;

    @BeforeEach
    void setUp() {
        List<DeviceCategory> categories = categoryRepository.findAllByActiveTrueOrderByNameAsc();
        smartphoneCategory = categories.stream().filter(c -> c.getSlug().equals("smartphones")).findFirst().orElseThrow();
        laptopCategory = categories.stream().filter(c -> c.getSlug().equals("laptops")).findFirst().orElseThrow();

        phoneScreenRepair = serviceRepository.findBySlugAndActiveTrueAndCategory_ActiveTrue("smartphone-screen-repair").orElseThrow();
        laptopScreenRepair = serviceRepository.findBySlugAndActiveTrueAndCategory_ActiveTrue("laptop-screen-repair").orElseThrow();
    }

    @Test
    void customerCanCreateBookingAndReceivesOfferOrNoMatch() throws Exception {
        CustomerAccount customer = createCustomer("customer-book@example.com", "Password-123");
        CustomerGadget gadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(customer, smartphoneCategory, "Google", "Pixel 8", null, null));

        TechnicianProfile tech = createTechnician("tech-pixel@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 3, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));

        MockHttpSession session = login("customer-book@example.com", "Password-123", "CUSTOMER");
        Csrf csrf = csrf(session);

        CreateBookingRequest request = new CreateBookingRequest(
                gadget.getId(),
                phoneScreenRepair.getId(),
                TechnicianServiceMode.HOME_VISIT,
                "Screen has horizontal lines after drop."
        );

        MvcResult result = mvc.perform(post("/api/bookings")
                        .session(session)
                        .cookie(csrf.cookie())
                        .header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OFFERED"))
                .andExpect(jsonPath("$.serviceMode").value("HOME_VISIT"))
                .andExpect(jsonPath("$.gadget.model").value("Pixel 8"))
                .andExpect(jsonPath("$.service.slug").value("smartphone-screen-repair"))
                .andExpect(jsonPath("$.assignedTechnician").doesNotExist())
                .andReturn();

        long bookingId = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Check that an offer was created for the technician
        List<AssignmentOffer> offers = offerRepository.findAllByBooking_Id(bookingId);
        assertEquals(1, offers.size());
        assertEquals(tech.getId(), offers.getFirst().getTechnician().getId());
        assertEquals(OfferStatus.OFFERED, offers.getFirst().getStatus());
        assertNotNull(offers.getFirst().getMatchScore());
        assertEquals(100.0, offers.getFirst().getMatchScore());
        assertNotNull(offers.getFirst().getMatchRationale());

        // Customer can view their booking
        mvc.perform(get("/api/bookings/" + bookingId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("OFFERED"));

        // Customer can list their bookings
        mvc.perform(get("/api/bookings").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void rejectsBookingWithIncompatibleCategoryOrUnownedGadget() throws Exception {
        CustomerAccount customer = createCustomer("owner-cust@example.com", "Password-123");
        CustomerAccount otherCust = createCustomer("other-cust@example.com", "Password-123");

        CustomerGadget phoneGadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(customer, smartphoneCategory, "Samsung", "Galaxy S22", null, null));
        CustomerGadget otherGadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(otherCust, smartphoneCategory, "OnePlus", "11", null, null));

        MockHttpSession session = login("owner-cust@example.com", "Password-123", "CUSTOMER");
        Csrf csrf = csrf(session);

        // 1. Attempting to book with another customer's gadget -> 400 Bad Request
        CreateBookingRequest unownedRequest = new CreateBookingRequest(
                otherGadget.getId(),
                phoneScreenRepair.getId(),
                TechnicianServiceMode.HOME_VISIT,
                "Broken screen please fix."
        );
        mvc.perform(post("/api/bookings").session(session).cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(unownedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The selected gadget does not belong to your account."));

        // 2. Category mismatch: Smartphone gadget with Laptop repair service -> 400 Bad Request
        CreateBookingRequest mismatchRequest = new CreateBookingRequest(
                phoneGadget.getId(),
                laptopScreenRepair.getId(),
                TechnicianServiceMode.HOME_VISIT,
                "Broken screen please fix."
        );
        mvc.perform(post("/api/bookings").session(session).cookie(csrf.cookie()).header(csrf.header(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(mismatchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void technicianAcceptsOfferAndAssignsBooking() throws Exception {
        CustomerAccount customer = createCustomer("cust-assign@example.com", "Password-123");
        CustomerGadget gadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(customer, smartphoneCategory, "Apple", "iPhone 14", null, null));

        TechnicianProfile tech = createTechnician("tech-accept@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 3, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));

        // Create booking directly
        RepairBooking booking = bookingRepository.saveAndFlush(
                new RepairBooking(customer, gadget, phoneScreenRepair, TechnicianServiceMode.HOME_VISIT, "Display cracked"));
        booking.markOffered();
        AssignmentOffer offer = offerRepository.saveAndFlush(new AssignmentOffer(booking, tech));

        MockHttpSession techSession = login("tech-accept@example.com", "Password-123", "TECHNICIAN");

        // Technician sees pending offer
        mvc.perform(get("/api/technician/assignments/offers").session(techSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(offer.getId()))
                .andExpect(jsonPath("$[0].status").value("OFFERED"))
                .andExpect(jsonPath("$[0].service.slug").value("smartphone-screen-repair"));

        Csrf techCsrf = csrf(techSession);

        // Accept offer
        mvc.perform(post("/api/technician/assignments/offers/" + offer.getId() + "/accept")
                        .session(techSession)
                        .cookie(techCsrf.cookie())
                        .header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Verify booking state in repository
        RepairBooking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.ASSIGNED, updatedBooking.getStatus());
        assertNotNull(updatedBooking.getAssignedTechnician());
        assertEquals(tech.getId(), updatedBooking.getAssignedTechnician().getId());

        // Technician sees assigned booking in active assignments
        mvc.perform(get("/api/technician/assignments").session(techSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId()))
                .andExpect(jsonPath("$[0].status").value("ASSIGNED"));

        // Pending offers list is now empty
        mvc.perform(get("/api/technician/assignments/offers").session(techSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Attempting to accept again returns 409 Conflict
        mvc.perform(post("/api/technician/assignments/offers/" + offer.getId() + "/accept")
                        .session(techSession)
                        .cookie(techCsrf.cookie())
                        .header(techCsrf.header(), techCsrf.token()))
                .andExpect(status().isConflict());
    }

    @Test
    void technicianRejectsOfferTriggeringSafeReassignmentAndExhaustingCandidates() throws Exception {
        CustomerAccount customer = createCustomer("cust-reassign@example.com", "Password-123");
        CustomerGadget gadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(customer, smartphoneCategory, "Apple", "iPhone 12", null, null));

        // Tech A (capacity 5 -> higher score) and Tech B (capacity 3)
        TechnicianProfile techA = createTechnician("tech-a@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 5, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));
        TechnicianProfile techB = createTechnician("tech-b@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 3, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));

        MockHttpSession custSession = login("cust-reassign@example.com", "Password-123", "CUSTOMER");
        Csrf custCsrf = csrf(custSession);

        // Customer creates booking -> Tech A should get initial offer
        MvcResult res = mvc.perform(post("/api/bookings").session(custSession).cookie(custCsrf.cookie()).header(custCsrf.header(), custCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CreateBookingRequest(
                                gadget.getId(), phoneScreenRepair.getId(), TechnicianServiceMode.HOME_VISIT, "Broken microphone"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OFFERED")).andReturn();

        long bookingId = mapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        // Confirm Tech A received the offer
        AssignmentOffer offerA = offerRepository.findByBooking_IdAndStatus(bookingId, OfferStatus.OFFERED).orElseThrow();
        assertEquals(techA.getId(), offerA.getTechnician().getId());

        // Tech A rejects offer
        MockHttpSession techASession = login("tech-a@example.com", "Password-123", "TECHNICIAN");
        Csrf techACsrf = csrf(techASession);
        mvc.perform(post("/api/technician/assignments/offers/" + offerA.getId() + "/reject")
                        .session(techASession).cookie(techACsrf.cookie()).header(techACsrf.header(), techACsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // Booking remains OFFERED, but now Tech B receives the second offer!
        RepairBooking bookingAfterFirstReject = bookingRepository.findById(bookingId).orElseThrow();
        assertEquals(BookingStatus.OFFERED, bookingAfterFirstReject.getStatus());

        AssignmentOffer offerB = offerRepository.findByBooking_IdAndStatus(bookingId, OfferStatus.OFFERED).orElseThrow();
        assertEquals(techB.getId(), offerB.getTechnician().getId());

        // Tech B rejects offer
        MockHttpSession techBSession = login("tech-b@example.com", "Password-123", "TECHNICIAN");
        Csrf techBCsrf = csrf(techBSession);
        mvc.perform(post("/api/technician/assignments/offers/" + offerB.getId() + "/reject")
                        .session(techBSession).cookie(techBCsrf.cookie()).header(techBCsrf.header(), techBCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // Now all candidates are exhausted -> Booking state is NO_MATCH
        RepairBooking bookingFinal = bookingRepository.findById(bookingId).orElseThrow();
        assertEquals(BookingStatus.NO_MATCH, bookingFinal.getStatus());
        assertNull(bookingFinal.getAssignedTechnician());
    }

    @Test
    void technicianCannotAcceptOrRejectAnotherTechniciansOffer() throws Exception {
        CustomerAccount customer = createCustomer("cust-iso@example.com", "Password-123");
        CustomerGadget gadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(customer, smartphoneCategory, "Xiaomi", "13 Pro", null, null));

        TechnicianProfile tech1 = createTechnician("tech-iso1@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 3, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));
        TechnicianProfile tech2 = createTechnician("tech-iso2@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 3, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(phoneScreenRepair));

        RepairBooking booking = bookingRepository.saveAndFlush(
                new RepairBooking(customer, gadget, phoneScreenRepair, TechnicianServiceMode.HOME_VISIT, "Screen issue"));
        booking.markOffered();
        AssignmentOffer offer = offerRepository.saveAndFlush(new AssignmentOffer(booking, tech1));

        // Tech 2 logs in and tries to act on Tech 1's offer -> 404 Not Found
        MockHttpSession tech2Session = login("tech-iso2@example.com", "Password-123", "TECHNICIAN");
        Csrf tech2Csrf = csrf(tech2Session);

        mvc.perform(post("/api/technician/assignments/offers/" + offer.getId() + "/accept")
                        .session(tech2Session).cookie(tech2Csrf.cookie()).header(tech2Csrf.header(), tech2Csrf.token()))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/technician/assignments/offers/" + offer.getId() + "/reject")
                        .session(tech2Session).cookie(tech2Csrf.cookie()).header(tech2Csrf.header(), tech2Csrf.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void customerCanRematchNoMatchBookingOnceTechnicianBecomesAvailable() throws Exception {
        CustomerAccount customer = createCustomer("rematch-cust@example.com", "Password-123");
        CustomerGadget gadget = gadgetRepository.saveAndFlush(
                new CustomerGadget(customer, laptopCategory, "Dell", "XPS 13", null, null));

        MockHttpSession custSession = login("rematch-cust@example.com", "Password-123", "CUSTOMER");
        Csrf custCsrf = csrf(custSession);

        // 1. Create booking when NO technician exists with laptopScreenRepair
        CreateBookingRequest req = new CreateBookingRequest(
                gadget.getId(), laptopScreenRepair.getId(), TechnicianServiceMode.HOME_VISIT, "Hinges and screen cracked"
        );
        MvcResult createRes = mvc.perform(post("/api/bookings").session(custSession)
                        .cookie(custCsrf.cookie()).header(custCsrf.header(), custCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NO_MATCH"))
                .andReturn();

        long bookingId = mapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // 2. Now a technician registers/activates with the required skill and service mode
        TechnicianProfile laptopTech = createTechnician("laptop-tech@example.com", "Password-123", true,
                TechnicianAvailability.AVAILABLE, 4, Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(laptopScreenRepair));

        // 3. Customer triggers rematch
        custCsrf = csrf(custSession);
        mvc.perform(post("/api/bookings/" + bookingId + "/rematch").session(custSession)
                        .cookie(custCsrf.cookie()).header(custCsrf.header(), custCsrf.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("OFFERED"));

        // 4. Technician sees the pending offer via GET /api/technician/assignments/offers
        MockHttpSession techSession = login("laptop-tech@example.com", "Password-123", "TECHNICIAN");
        mvc.perform(get("/api/technician/assignments/offers").session(techSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(bookingId))
                .andExpect(jsonPath("$[0].status").value("OFFERED"))
                .andExpect(jsonPath("$[0].matchScore").value(100.0))
                .andExpect(jsonPath("$[0].matchRationale").exists());
    }

    private CustomerAccount createCustomer(String email, String password) {
        return accountRepository.saveAndFlush(new CustomerAccount(
                "Customer", email, passwordEncoder.encode(password), CustomerRole.CUSTOMER
        ));
    }

    private TechnicianProfile createTechnician(String email, String password, boolean active,
                                               TechnicianAvailability availability, int capacityLimit,
                                               Set<TechnicianServiceMode> modes, Set<RepairService> skills) {
        CustomerAccount account = accountRepository.saveAndFlush(new CustomerAccount(
                "Technician", email, passwordEncoder.encode(password), CustomerRole.TECHNICIAN
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
