package com.gadgetgo.booking.service;

import com.gadgetgo.booking.api.dto.BookingResponse;
import com.gadgetgo.booking.api.dto.TechnicianOfferResponse;
import com.gadgetgo.booking.domain.AssignmentOffer;
import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.OfferStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.repository.AssignmentOfferRepository;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TechnicianAssignmentService {

    private final AssignmentOfferRepository offerRepository;
    private final RepairBookingRepository bookingRepository;
    private final TechnicianMatchingService matchingService;

    public TechnicianAssignmentService(AssignmentOfferRepository offerRepository,
                                       RepairBookingRepository bookingRepository,
                                       TechnicianMatchingService matchingService) {
        this.offerRepository = offerRepository;
        this.bookingRepository = bookingRepository;
        this.matchingService = matchingService;
    }

    public List<TechnicianOfferResponse> listPendingOffers(Long technicianAccountId) {
        return offerRepository.findAllByTechnician_Account_IdAndStatusOrderByCreatedAtDesc(technicianAccountId, OfferStatus.OFFERED).stream()
                .map(TechnicianOfferResponse::from)
                .toList();
    }

    public List<BookingResponse> listAssignedBookings(Long technicianAccountId) {
        return bookingRepository.findAllByAssignedTechnician_Account_IdAndStatusInOrderByCreatedAtDesc(
                technicianAccountId, TechnicianMatchingService.ACTIVE_WORKLOAD_STATUSES).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public TechnicianOfferResponse acceptOffer(Long technicianAccountId, Long offerId) {
        AssignmentOffer offer = offerRepository.findWithLockByIdAndTechnician_Account_Id(offerId, technicianAccountId)
                .orElseThrow(OfferNotFoundException::new);

        if (offer.getStatus() != OfferStatus.OFFERED) {
            throw new InvalidOfferStateException("This assignment offer is no longer open for response.");
        }

        RepairBooking booking = bookingRepository.findWithLockById(offer.getBooking().getId())
                .orElseThrow(BookingNotFoundException::new);

        if (booking.getStatus() != BookingStatus.OFFERED) {
            throw new InvalidOfferStateException("This booking is no longer available for assignment.");
        }

        TechnicianProfile technician = offer.getTechnician();

        if (!technician.isActive() || technician.getAccount() == null || !technician.getAccount().isActive()) {
            throw new InvalidOfferStateException("Technician account is not active.");
        }

        if (technician.getAvailability() != TechnicianAvailability.AVAILABLE) {
            throw new InvalidOfferStateException("Technician is not currently marked available.");
        }

        int currentWorkload = (int) bookingRepository
                .countByAssignedTechnician_IdAndStatusIn(technician.getId(), TechnicianMatchingService.ACTIVE_WORKLOAD_STATUSES);
        if (currentWorkload >= technician.getCapacityLimit()) {
            throw new InvalidOfferStateException("Technician capacity limit has been reached.");
        }

        offer.accept();
        booking.markAssigned(technician);

        return TechnicianOfferResponse.from(offer);
    }

    @Transactional
    public TechnicianOfferResponse rejectOffer(Long technicianAccountId, Long offerId) {
        AssignmentOffer offer = offerRepository.findWithLockByIdAndTechnician_Account_Id(offerId, technicianAccountId)
                .orElseThrow(OfferNotFoundException::new);

        if (offer.getStatus() != OfferStatus.OFFERED) {
            throw new InvalidOfferStateException("This assignment offer is no longer open for response.");
        }

        RepairBooking booking = bookingRepository.findWithLockById(offer.getBooking().getId())
                .orElseThrow(BookingNotFoundException::new);

        if (booking.getStatus() != BookingStatus.OFFERED) {
            throw new InvalidOfferStateException("This booking is no longer in an offered state.");
        }

        offer.reject();

        List<Long> previouslyOfferedIds = offerRepository.findOfferedTechnicianProfileIdsByBookingId(booking.getId());
        Set<Long> excluded = new HashSet<>(previouslyOfferedIds);
        excluded.add(offer.getTechnician().getId());

        var nextMatch = matchingService.findBestMatch(booking, excluded);
        if (nextMatch.isPresent()) {
            var match = nextMatch.get();
            AssignmentOffer nextOffer = new AssignmentOffer(booking, match.technician(), match.totalScore(), match.rationale());
            offerRepository.save(nextOffer);
        } else {
            booking.markNoMatch();
        }

        return TechnicianOfferResponse.from(offer);
    }
}
