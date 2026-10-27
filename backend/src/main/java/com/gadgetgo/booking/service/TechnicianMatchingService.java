package com.gadgetgo.booking.service;

import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.domain.TechnicianServiceMode;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TechnicianMatchingService {

    public static final double SKILL_WEIGHT = 0.50;
    public static final double AVAILABILITY_WEIGHT = 0.30;
    public static final double WORKLOAD_WEIGHT = 0.20;

    private final TechnicianProfileRepository technicianRepository;
    private final RepairBookingRepository bookingRepository;

    public static final Set<BookingStatus> ACTIVE_WORKLOAD_STATUSES = Set.of(
            BookingStatus.ASSIGNED,
            BookingStatus.DIAGNOSED,
            BookingStatus.QUOTED,
            BookingStatus.APPROVED,
            BookingStatus.IN_PROGRESS
    );

    public TechnicianMatchingService(TechnicianProfileRepository technicianRepository,
                                     RepairBookingRepository bookingRepository) {
        this.technicianRepository = technicianRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Evaluates all candidate technicians against hard eligibility criteria,
     * scores eligible candidates, and returns them sorted deterministically.
     */
    public List<TechnicianMatchResult> evaluateCandidates(RepairBooking booking, Set<Long> excludedTechnicianProfileIds) {
        Long targetServiceId = booking.getRepairService().getId();
        TechnicianServiceMode targetMode = booking.getServiceMode();

        List<TechnicianProfile> candidates = technicianRepository
                .findAllByActiveTrueAndAccount_ActiveTrueAndAvailability(TechnicianAvailability.AVAILABLE);

        List<TechnicianMatchResult> results = new ArrayList<>();

        for (TechnicianProfile tech : candidates) {
            // Hard eligibility checks
            if (!tech.isActive() || tech.getAccount() == null || !tech.getAccount().isActive()) {
                continue;
            }
            if (tech.getAvailability() != TechnicianAvailability.AVAILABLE) {
                continue;
            }
            if (excludedTechnicianProfileIds != null && excludedTechnicianProfileIds.contains(tech.getId())) {
                continue;
            }
            if (!tech.getServiceModes().contains(targetMode)) {
                continue;
            }
            boolean hasSkill = tech.getSkills().stream().anyMatch(skill ->
                    skill.getId().equals(targetServiceId) && skill.isActive() && skill.getCategory().isActive());
            if (!hasSkill) {
                continue;
            }

            int capacityLimit = tech.getCapacityLimit();
            int currentWorkload = (int) bookingRepository
                    .countByAssignedTechnician_IdAndStatus(tech.getId(), BookingStatus.ASSIGNED);

            if (currentWorkload >= capacityLimit) {
                continue;
            }

            // Deterministic scoring
            double skillScore = 100.0;
            double availabilityScore = 100.0;
            double workloadScore = Math.max(0.0, Math.min(100.0,
                    100.0 * (capacityLimit - currentWorkload) / (double) capacityLimit));

            double totalScore = (SKILL_WEIGHT * skillScore)
                    + (AVAILABILITY_WEIGHT * availabilityScore)
                    + (WORKLOAD_WEIGHT * workloadScore);

            String rationale = String.format(
                    "Match Score: %.1f%% · Skill: %.0f%% (wt 50%%), Availability: %.0f%% (wt 30%%), Remaining Capacity: %.0f%% (%d/%d slots free, wt 20%%)",
                    totalScore,
                    skillScore,
                    availabilityScore,
                    workloadScore,
                    capacityLimit - currentWorkload,
                    capacityLimit
            );

            results.add(new TechnicianMatchResult(
                    tech,
                    totalScore,
                    skillScore,
                    availabilityScore,
                    workloadScore,
                    currentWorkload,
                    capacityLimit,
                    rationale
            ));
        }

        // Rank deterministically: highest score first, tie-break by ascending technician profile ID
        results.sort(Comparator.comparingDouble(TechnicianMatchResult::totalScore).reversed()
                .thenComparing(res -> res.technician().getId()));

        return results;
    }

    /**
     * Returns the highest-ranked eligible candidate, or empty if no candidate matches.
     */
    public Optional<TechnicianMatchResult> findBestMatch(RepairBooking booking, Set<Long> excludedTechnicianProfileIds) {
        List<TechnicianMatchResult> ranked = evaluateCandidates(booking, excludedTechnicianProfileIds);
        return ranked.isEmpty() ? Optional.empty() : Optional.of(ranked.getFirst());
    }

    public record TechnicianMatchResult(
            TechnicianProfile technician,
            double totalScore,
            double skillScore,
            double availabilityScore,
            double workloadScore,
            int currentWorkload,
            int capacityLimit,
            String rationale
    ) { }
}
