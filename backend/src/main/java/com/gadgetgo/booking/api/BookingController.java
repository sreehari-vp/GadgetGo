package com.gadgetgo.booking.api;

import com.gadgetgo.booking.api.dto.BookingResponse;
import com.gadgetgo.booking.api.dto.CreateBookingRequest;
import com.gadgetgo.booking.api.dto.CustomerQuotationDecisionRequest;
import com.gadgetgo.booking.api.dto.JobDetailResponse;
import com.gadgetgo.booking.api.dto.QuotationResponse;
import com.gadgetgo.booking.service.BookingService;
import com.gadgetgo.booking.service.RepairWorkflowService;
import com.gadgetgo.security.CustomerPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;
    private final RepairWorkflowService workflowService;

    public BookingController(BookingService bookingService, RepairWorkflowService workflowService) {
        this.bookingService = bookingService;
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<BookingResponse> list(@AuthenticationPrincipal CustomerPrincipal principal) {
        return bookingService.listBookings(principal.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal CustomerPrincipal principal,
                                  @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(principal.id(), request);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@AuthenticationPrincipal CustomerPrincipal principal,
                               @PathVariable @Positive Long id) {
        return bookingService.getBooking(principal.id(), id);
    }

    @GetMapping("/{id}/detail")
    public JobDetailResponse getDetail(@AuthenticationPrincipal CustomerPrincipal principal,
                                       @PathVariable @Positive Long id) {
        return workflowService.getJobDetailForCustomer(principal.id(), id);
    }

    @PostMapping("/{id}/rematch")
    public BookingResponse rematch(@AuthenticationPrincipal CustomerPrincipal principal,
                                   @PathVariable @Positive Long id) {
        return bookingService.rematchBooking(principal.id(), id);
    }

    @PostMapping("/{id}/quotation/approve")
    public QuotationResponse approveQuotation(@AuthenticationPrincipal CustomerPrincipal principal,
                                             @PathVariable @Positive Long id) {
        return workflowService.approveQuotation(principal.id(), id);
    }

    @PostMapping("/{id}/quotation/reject")
    public QuotationResponse rejectQuotation(@AuthenticationPrincipal CustomerPrincipal principal,
                                             @PathVariable @Positive Long id,
                                             @RequestBody(required = false) CustomerQuotationDecisionRequest request) {
        String notes = request != null ? request.notes() : null;
        return workflowService.rejectQuotation(principal.id(), id, notes);
    }
}
