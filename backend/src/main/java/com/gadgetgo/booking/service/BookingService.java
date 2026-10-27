package com.gadgetgo.booking.service;

import com.gadgetgo.booking.api.dto.BookingResponse;
import com.gadgetgo.booking.api.dto.CreateBookingRequest;
import com.gadgetgo.booking.domain.AssignmentOffer;
import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.repository.AssignmentOfferRepository;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.repository.RepairServiceRepository;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.gadget.domain.CustomerGadget;
import com.gadgetgo.gadget.repository.CustomerGadgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final RepairBookingRepository bookingRepository;
    private final AssignmentOfferRepository offerRepository;
    private final CustomerAccountRepository customerRepository;
    private final CustomerGadgetRepository gadgetRepository;
    private final RepairServiceRepository repairServiceRepository;
    private final TechnicianMatchingService matchingService;

    public BookingService(RepairBookingRepository bookingRepository,
                          AssignmentOfferRepository offerRepository,
                          CustomerAccountRepository customerRepository,
                          CustomerGadgetRepository gadgetRepository,
                          RepairServiceRepository repairServiceRepository,
                          TechnicianMatchingService matchingService) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
        this.gadgetRepository = gadgetRepository;
        this.repairServiceRepository = repairServiceRepository;
        this.matchingService = matchingService;
    }

    public List<BookingResponse> listBookings(Long customerId) {
        return bookingRepository.findAllByCustomer_IdOrderByCreatedAtDescIdDesc(customerId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse getBooking(Long customerId, Long bookingId) {
        return bookingRepository.findByIdAndCustomer_Id(bookingId, customerId)
                .map(BookingResponse::from)
                .orElseThrow(BookingNotFoundException::new);
    }

    @Transactional
    public BookingResponse createBooking(Long customerId, CreateBookingRequest request) {
        CustomerAccount customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new InvalidBookingException("Customer account not found."));

        CustomerGadget gadget = gadgetRepository.findByIdAndCustomer_Id(request.gadgetId(), customerId)
                .orElseThrow(() -> new InvalidBookingException("The selected gadget does not belong to your account."));

        RepairService repairService = repairServiceRepository.findById(request.serviceId())
                .filter(s -> s.isActive() && s.getCategory().isActive())
                .orElseThrow(() -> new InvalidBookingException("The selected repair service is inactive or not found."));

        if (!gadget.getCategory().getId().equals(repairService.getCategory().getId())) {
            throw new InvalidBookingException(String.format(
                    "Gadget category '%s' is not compatible with repair service category '%s'.",
                    gadget.getCategory().getName(),
                    repairService.getCategory().getName()
            ));
        }

        RepairBooking booking = new RepairBooking(
                customer,
                gadget,
                repairService,
                request.serviceMode(),
                request.problemDescription().trim()
        );

        booking = bookingRepository.saveAndFlush(booking);

        var bestMatch = matchingService.findBestMatch(booking, Set.of());
        if (bestMatch.isPresent()) {
            var match = bestMatch.get();
            AssignmentOffer offer = new AssignmentOffer(booking, match.technician(), match.totalScore(), match.rationale());
            offerRepository.save(offer);
            booking.markOffered();
        } else {
            booking.markNoMatch();
        }

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse rematchBooking(Long customerId, Long bookingId) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .filter(b -> b.getCustomer().getId().equals(customerId))
                .orElseThrow(BookingNotFoundException::new);

        if (booking.getStatus() == BookingStatus.ASSIGNED) {
            throw new InvalidBookingException("This booking is already assigned to a technician.");
        }

        List<Long> previouslyOfferedIds = offerRepository.findOfferedTechnicianProfileIdsByBookingId(bookingId);
        var bestMatch = matchingService.findBestMatch(booking, new java.util.HashSet<>(previouslyOfferedIds));
        if (bestMatch.isPresent()) {
            var match = bestMatch.get();
            AssignmentOffer offer = new AssignmentOffer(booking, match.technician(), match.totalScore(), match.rationale());
            offerRepository.save(offer);
            booking.markOffered();
        } else {
            booking.markNoMatch();
        }

        return BookingResponse.from(booking);
    }
}
