package com.gadgetgo.booking.api;

import com.gadgetgo.booking.api.dto.BookingResponse;
import com.gadgetgo.booking.api.dto.TechnicianOfferResponse;
import com.gadgetgo.booking.service.TechnicianAssignmentService;
import com.gadgetgo.security.CustomerPrincipal;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technician/assignments")
@Validated
public class TechnicianAssignmentController {

    private final TechnicianAssignmentService assignmentService;

    public TechnicianAssignmentController(TechnicianAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/offers")
    public List<TechnicianOfferResponse> listOffers(@AuthenticationPrincipal CustomerPrincipal principal) {
        return assignmentService.listPendingOffers(principal.id());
    }

    @PostMapping("/offers/{id}/accept")
    public TechnicianOfferResponse acceptOffer(@AuthenticationPrincipal CustomerPrincipal principal,
                                               @PathVariable @Positive Long id) {
        return assignmentService.acceptOffer(principal.id(), id);
    }

    @PostMapping("/offers/{id}/reject")
    public TechnicianOfferResponse rejectOffer(@AuthenticationPrincipal CustomerPrincipal principal,
                                               @PathVariable @Positive Long id) {
        return assignmentService.rejectOffer(principal.id(), id);
    }

    @GetMapping
    public List<BookingResponse> listAssigned(@AuthenticationPrincipal CustomerPrincipal principal) {
        return assignmentService.listAssignedBookings(principal.id());
    }
}
