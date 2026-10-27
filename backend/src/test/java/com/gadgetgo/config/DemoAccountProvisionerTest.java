package com.gadgetgo.config;

import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.repository.AssignmentOfferRepository;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.booking.service.TechnicianMatchingService;
import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.repository.RepairServiceRepository;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.gadget.domain.CustomerGadget;
import com.gadgetgo.gadget.repository.CustomerGadgetRepository;
import com.gadgetgo.supplier.domain.SupplierProfile;
import com.gadgetgo.supplier.repository.SupplierProfileRepository;
import com.gadgetgo.support.TestDatabaseSafetyInitializer;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.domain.TechnicianServiceMode;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseSafetyInitializer.class)
@Transactional
class DemoAccountProvisionerTest {

    @Autowired private CustomerAccountRepository accountRepository;
    @Autowired private TechnicianProfileRepository technicianRepository;
    @Autowired private SupplierProfileRepository supplierRepository;
    @Autowired private RepairServiceRepository repairServiceRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TechnicianMatchingService matchingService;
    @Autowired private CustomerGadgetRepository gadgetRepository;
    @Autowired private RepairBookingRepository bookingRepository;
    @Autowired private AssignmentOfferRepository offerRepository;

    private MockEnvironment environment;
    private DemoAccountProvisioner provisioner;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        environment.setProperty("DEMO_DEFAULT_PASSWORD", "LocalTestPassword123!");
        provisioner = new DemoAccountProvisioner(
                accountRepository,
                technicianRepository,
                supplierRepository,
                repairServiceRepository,
                passwordEncoder,
                environment
        );
    }

    @Test
    void testProvisioningCreatesFiveTechniciansAndFiveSuppliers() {
        provisioner.run(null);

        // Verify 5 Technician Accounts & Profiles
        List<String> expectedTechEmails = List.of(
                "alex.rivera@gadgetgo.local",
                "sam.chen@gadgetgo.local",
                "jordan.taylor@gadgetgo.local",
                "morgan.patel@gadgetgo.local",
                "casey.kim@gadgetgo.local"
        );

        for (String email : expectedTechEmails) {
            Optional<CustomerAccount> optAccount = accountRepository.findByEmail(email);
            assertTrue(optAccount.isPresent(), "Account should exist for " + email);
            CustomerAccount account = optAccount.get();
            assertEquals(CustomerRole.TECHNICIAN, account.getRole());
            assertTrue(passwordEncoder.matches("LocalTestPassword123!", account.getPasswordHash()));

            Optional<TechnicianProfile> optProfile = technicianRepository.findByAccount_Id(account.getId());
            assertTrue(optProfile.isPresent(), "Technician profile should exist for " + email);
            TechnicianProfile profile = optProfile.get();
            assertTrue(profile.isActive());
            assertEquals(TechnicianAvailability.AVAILABLE, profile.getAvailability());
            assertTrue(profile.getCapacityLimit() >= 3 && profile.getCapacityLimit() <= 5);
            assertFalse(profile.getServiceModes().isEmpty(), "Service modes should not be empty");
            assertFalse(profile.getSkills().isEmpty(), "Skills should not be empty");
        }

        // Verify specific service mode configuration
        TechnicianProfile morgan = technicianRepository.findByAccount_Id(
                accountRepository.findByEmail("morgan.patel@gadgetgo.local").orElseThrow().getId()).orElseThrow();
        assertEquals(Set.of(TechnicianServiceMode.HOME_VISIT), morgan.getServiceModes());

        TechnicianProfile jordan = technicianRepository.findByAccount_Id(
                accountRepository.findByEmail("jordan.taylor@gadgetgo.local").orElseThrow().getId()).orElseThrow();
        assertEquals(Set.of(TechnicianServiceMode.REPAIR_CENTRE), jordan.getServiceModes());

        // Verify 5 Supplier Accounts & Profiles
        List<String> expectedSupplierEmails = List.of(
                "marcus.vance@gadgetgo.local",
                "elena.rostova@gadgetgo.local",
                "david.oconnor@gadgetgo.local",
                "amina.diallo@gadgetgo.local",
                "lucas.lindqvist@gadgetgo.local"
        );

        for (String email : expectedSupplierEmails) {
            Optional<CustomerAccount> optAccount = accountRepository.findByEmail(email);
            assertTrue(optAccount.isPresent(), "Account should exist for " + email);
            CustomerAccount account = optAccount.get();
            assertEquals(CustomerRole.SUPPLIER, account.getRole());
            assertTrue(passwordEncoder.matches("LocalTestPassword123!", account.getPasswordHash()));

            Optional<SupplierProfile> optProfile = supplierRepository.findByAccount_Id(account.getId());
            assertTrue(optProfile.isPresent(), "Supplier profile should exist for " + email);
            SupplierProfile profile = optProfile.get();
            assertTrue(profile.isActive());
            assertNotNull(profile.getBusinessName());
            assertFalse(profile.getBusinessName().isBlank());
        }
    }

    @Test
    void testProvisioningIsIdempotent() {
        // Run first time
        provisioner.run(null);
        long techCount1 = technicianRepository.count();
        long suppCount1 = supplierRepository.count();

        // Run second time
        assertDoesNotThrow(() -> provisioner.run(null));
        long techCount2 = technicianRepository.count();
        long suppCount2 = supplierRepository.count();

        assertEquals(techCount1, techCount2, "Repeated execution should not duplicate technicians");
        assertEquals(suppCount1, suppCount2, "Repeated execution should not duplicate suppliers");
    }

    @Test
    void testProvisioningUpdatesPasswordWhenConfiguredPasswordChanges() {
        provisioner.run(null);

        CustomerAccount account = accountRepository.findByEmail("alex.rivera@gadgetgo.local").orElseThrow();
        assertTrue(passwordEncoder.matches("LocalTestPassword123!", account.getPasswordHash()));

        // Change password in environment
        environment.setProperty("DEMO_DEFAULT_PASSWORD", "NewUpdatedPassword456!");
        provisioner.run(null);

        CustomerAccount updatedAccount = accountRepository.findByEmail("alex.rivera@gadgetgo.local").orElseThrow();
        assertTrue(passwordEncoder.matches("NewUpdatedPassword456!", updatedAccount.getPasswordHash()),
                "Password hash should be updated to match the new DEMO_DEFAULT_PASSWORD");
    }

    @Test
    void testPasswordValidationFailsWhenMissingOrShort() {
        MockEnvironment emptyEnv = new MockEnvironment();
        DemoAccountProvisioner unconfiguredProvisioner = new DemoAccountProvisioner(
                accountRepository, technicianRepository, supplierRepository, repairServiceRepository, passwordEncoder, emptyEnv
        );

        IllegalStateException exMissing = assertThrows(IllegalStateException.class, () -> unconfiguredProvisioner.run(null));
        assertTrue(exMissing.getMessage().contains("must be configured"));

        MockEnvironment shortEnv = new MockEnvironment();
        shortEnv.setProperty("DEMO_DEFAULT_PASSWORD", "short");
        DemoAccountProvisioner shortProvisioner = new DemoAccountProvisioner(
                accountRepository, technicianRepository, supplierRepository, repairServiceRepository, passwordEncoder, shortEnv
        );

        IllegalStateException exShort = assertThrows(IllegalStateException.class, () -> shortProvisioner.run(null));
        assertTrue(exShort.getMessage().contains("must contain 10–72 UTF-8 bytes"));
    }

    @Test
    void testProvisionedTechniciansMatchStage5BRepairBooking() {
        // Provision the demo accounts
        provisioner.run(null);

        // Fetch a catalogue service covered by Alex, Sam, Morgan, and Casey
        RepairService screenRepairService = repairServiceRepository
                .findBySlugAndActiveTrueAndCategory_ActiveTrue("smartphone-screen-repair")
                .orElseThrow();

        // Create a customer & gadget
        CustomerAccount customer = accountRepository.save(new CustomerAccount("Test Customer", "cust.test@gadgetgo.local", "hashedpass1234", CustomerRole.CUSTOMER));
        CustomerGadget gadget = gadgetRepository.save(new CustomerGadget(customer, screenRepairService.getCategory(), "Google", "Pixel 8", null, null));

        // Create booking in HOME_VISIT mode
        RepairBooking booking = bookingRepository.save(new RepairBooking(
                customer,
                gadget,
                screenRepairService,
                TechnicianServiceMode.HOME_VISIT,
                "Screen is cracked and unresponsive"
        ));

        // Matching candidates should find eligible demo technicians
        List<TechnicianMatchingService.TechnicianMatchResult> candidates = matchingService.evaluateCandidates(booking, Set.of());
        assertFalse(candidates.isEmpty(), "Candidates should be found among provisioned technicians");

        // Top candidate should be eligible
        TechnicianMatchingService.TechnicianMatchResult topCandidate = candidates.get(0);
        assertTrue(topCandidate.totalScore() > 0);
        assertTrue(topCandidate.technician().getServiceModes().contains(TechnicianServiceMode.HOME_VISIT));
        assertTrue(topCandidate.technician().getSkills().contains(screenRepairService));

        // If the top candidate is excluded (e.g. after rejecting), a second candidate is found (reassignment)
        List<TechnicianMatchingService.TechnicianMatchResult> reassignmentCandidates =
                matchingService.evaluateCandidates(booking, Set.of(topCandidate.technician().getId()));
        assertFalse(reassignmentCandidates.isEmpty(), "Reassignment candidates should be found among remaining provisioned technicians");
        assertNotEquals(topCandidate.technician().getId(), reassignmentCandidates.get(0).technician().getId());
    }
}
