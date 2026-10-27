package com.gadgetgo.booking.service;

import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.catalog.domain.DeviceCategory;
import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.domain.RepairType;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.gadget.domain.CustomerGadget;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.domain.TechnicianServiceMode;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicianMatchingServiceTest {

    @Mock
    private TechnicianProfileRepository technicianRepository;

    @Mock
    private RepairBookingRepository bookingRepository;

    private TechnicianMatchingService matchingService;

    private DeviceCategory category;
    private RepairService screenRepairService;
    private RepairService batteryService;
    private CustomerAccount customer;
    private CustomerGadget gadget;
    private RepairBooking booking;

    @BeforeEach
    void setUp() throws Exception {
        matchingService = new TechnicianMatchingService(technicianRepository, bookingRepository);

        category = org.springframework.beans.BeanUtils.instantiateClass(DeviceCategory.class.getDeclaredConstructor());
        ReflectionTestUtils.setField(category, "id", 1L);
        ReflectionTestUtils.setField(category, "slug", "smartphones");
        ReflectionTestUtils.setField(category, "name", "Smartphones");
        ReflectionTestUtils.setField(category, "active", true);

        screenRepairService = org.springframework.beans.BeanUtils.instantiateClass(RepairService.class.getDeclaredConstructor());
        ReflectionTestUtils.setField(screenRepairService, "id", 10L);
        ReflectionTestUtils.setField(screenRepairService, "slug", "smartphone-screen-repair");
        ReflectionTestUtils.setField(screenRepairService, "name", "Screen Repair");
        ReflectionTestUtils.setField(screenRepairService, "category", category);
        ReflectionTestUtils.setField(screenRepairService, "repairType", RepairType.SCREEN_REPAIR);
        ReflectionTestUtils.setField(screenRepairService, "active", true);

        batteryService = org.springframework.beans.BeanUtils.instantiateClass(RepairService.class.getDeclaredConstructor());
        ReflectionTestUtils.setField(batteryService, "id", 20L);
        ReflectionTestUtils.setField(batteryService, "slug", "smartphone-battery-replacement");
        ReflectionTestUtils.setField(batteryService, "name", "Battery Replacement");
        ReflectionTestUtils.setField(batteryService, "category", category);
        ReflectionTestUtils.setField(batteryService, "repairType", RepairType.BATTERY_REPLACEMENT);
        ReflectionTestUtils.setField(batteryService, "active", true);

        customer = new CustomerAccount("John Doe", "john@example.com", "hash", CustomerRole.CUSTOMER);
        ReflectionTestUtils.setField(customer, "id", 100L);

        gadget = new CustomerGadget(customer, category, "Apple", "iPhone 13", null, null);
        ReflectionTestUtils.setField(gadget, "id", 500L);

        booking = new RepairBooking(customer, gadget, screenRepairService, TechnicianServiceMode.HOME_VISIT, "Screen cracked");
        ReflectionTestUtils.setField(booking, "id", 1000L);
    }

    private TechnicianProfile createTech(Long id, boolean active, boolean accountActive,
                                         TechnicianAvailability availability, int capacityLimit,
                                         Set<TechnicianServiceMode> modes, Set<RepairService> skills) {
        CustomerAccount account = new CustomerAccount("Tech " + id, "tech" + id + "@example.com", "hash", CustomerRole.TECHNICIAN);
        ReflectionTestUtils.setField(account, "id", id + 1000);
        ReflectionTestUtils.setField(account, "active", accountActive);

        TechnicianProfile profile = new TechnicianProfile(account, active, "District 1", availability, capacityLimit);
        ReflectionTestUtils.setField(profile, "id", id);
        profile.setServiceModes(modes);
        profile.setSkills(skills);
        return profile;
    }

    @Test
    void selectsEligibleTechnicianWithHighestScore() {
        // Tech 1: capacity 5, workload 0 -> workloadScore = 100 -> total = 100.0
        TechnicianProfile tech1 = createTech(1L, true, true, TechnicianAvailability.AVAILABLE, 5,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        // Tech 2: capacity 4, workload 2 -> workloadScore = 50 -> total = 50 + 30 + 10 = 90.0
        TechnicianProfile tech2 = createTech(2L, true, true, TechnicianAvailability.AVAILABLE, 4,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech1, tech2));
        when(bookingRepository.countByAssignedTechnician_IdAndStatus(1L, BookingStatus.ASSIGNED)).thenReturn(0L);
        when(bookingRepository.countByAssignedTechnician_IdAndStatus(2L, BookingStatus.ASSIGNED)).thenReturn(2L);

        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of());

        assertTrue(match.isPresent());
        assertEquals(1L, match.get().technician().getId());
        assertEquals(100.0, match.get().totalScore());
        assertEquals(100.0, match.get().skillScore());
        assertEquals(100.0, match.get().availabilityScore());
        assertEquals(100.0, match.get().workloadScore());
    }

    @Test
    void breaksTiesDeterministicallyByAscendingTechnicianId() {
        // Tech 10 and Tech 5 have identical scores (capacity 4, workload 1 -> 75% workload -> score = 95.0)
        TechnicianProfile tech10 = createTech(10L, true, true, TechnicianAvailability.AVAILABLE, 4,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));
        TechnicianProfile tech5 = createTech(5L, true, true, TechnicianAvailability.AVAILABLE, 4,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech10, tech5));
        when(bookingRepository.countByAssignedTechnician_IdAndStatus(any(), eq(BookingStatus.ASSIGNED))).thenReturn(1L);

        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of());

        assertTrue(match.isPresent());
        // Tech 5 must be chosen over Tech 10 because 5 < 10
        assertEquals(5L, match.get().technician().getId());
        assertEquals(95.0, match.get().totalScore());
    }

    @Test
    void excludesTechnicianMissingRequiredSkill() {
        // Tech only has battery replacement, not screen repair
        TechnicianProfile tech = createTech(1L, true, true, TechnicianAvailability.AVAILABLE, 3,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(batteryService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech));

        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of());
        assertTrue(match.isEmpty());
    }

    @Test
    void excludesTechnicianWithUnsupportedServiceMode() {
        // Booking requires HOME_VISIT, tech only offers REPAIR_CENTRE
        TechnicianProfile tech = createTech(1L, true, true, TechnicianAvailability.AVAILABLE, 3,
                Set.of(TechnicianServiceMode.REPAIR_CENTRE), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech));

        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of());
        assertTrue(match.isEmpty());
    }

    @Test
    void excludesInactiveProfileOrAccount() {
        TechnicianProfile techInactiveProfile = createTech(1L, false, true, TechnicianAvailability.AVAILABLE, 3,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));
        TechnicianProfile techInactiveAccount = createTech(2L, true, false, TechnicianAvailability.AVAILABLE, 3,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(techInactiveProfile, techInactiveAccount));

        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of());
        assertTrue(match.isEmpty());
    }

    @Test
    void excludesCapacityExhaustedTechnician() {
        // Capacity 2, currently assigned 2
        TechnicianProfile tech = createTech(1L, true, true, TechnicianAvailability.AVAILABLE, 2,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech));
        when(bookingRepository.countByAssignedTechnician_IdAndStatus(1L, BookingStatus.ASSIGNED)).thenReturn(2L);

        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of());
        assertTrue(match.isEmpty());
    }

    @Test
    void excludesPreviouslyOfferedTechnician() {
        TechnicianProfile tech = createTech(1L, true, true, TechnicianAvailability.AVAILABLE, 5,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech));

        // When tech 1L is in excluded set (previously offered/rejected)
        Optional<TechnicianMatchingService.TechnicianMatchResult> match = matchingService.findBestMatch(booking, Set.of(1L));
        assertTrue(match.isEmpty());
    }

    @Test
    void workloadScoreStaysWithinZeroToHundred() {
        TechnicianProfile tech = createTech(1L, true, true, TechnicianAvailability.AVAILABLE, 3,
                Set.of(TechnicianServiceMode.HOME_VISIT), Set.of(screenRepairService));

        when(technicianRepository.findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE))
                .thenReturn(List.of(tech));
        when(bookingRepository.countByAssignedTechnician_IdAndStatus(1L, BookingStatus.ASSIGNED)).thenReturn(0L);

        var list = matchingService.evaluateCandidates(booking, Set.of());
        assertEquals(1, list.size());
        assertTrue(list.get(0).workloadScore() >= 0.0 && list.get(0).workloadScore() <= 100.0);
    }
}
