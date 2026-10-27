package com.gadgetgo.booking.api;

import com.gadgetgo.booking.api.dto.BookingResponse;
import com.gadgetgo.booking.api.dto.JobDetailResponse;
import com.gadgetgo.booking.api.dto.QuotationRequest;
import com.gadgetgo.booking.api.dto.QuotationResponse;
import com.gadgetgo.booking.service.RepairWorkflowService;
import com.gadgetgo.booking.service.TechnicianAssignmentService;
import com.gadgetgo.security.CustomerPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/technician/jobs")
@Validated
public class TechnicianJobController {

    private final TechnicianAssignmentService assignmentService;
    private final RepairWorkflowService workflowService;

    public TechnicianJobController(TechnicianAssignmentService assignmentService,
                                   RepairWorkflowService workflowService) {
        this.assignmentService = assignmentService;
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<BookingResponse> listAssignedJobs(@AuthenticationPrincipal CustomerPrincipal principal) {
        return assignmentService.listAssignedBookings(principal.id());
    }

    @GetMapping("/{id}")
    public JobDetailResponse getJobDetail(@AuthenticationPrincipal CustomerPrincipal principal,
                                          @PathVariable @Positive Long id) {
        return workflowService.getJobDetailForTechnician(principal.id(), id);
    }

    @PostMapping("/{id}/quotation/draft")
    public QuotationResponse saveDraft(@AuthenticationPrincipal CustomerPrincipal principal,
                                       @PathVariable @Positive Long id,
                                       @Valid @RequestBody QuotationRequest request) {
        return workflowService.saveQuotationDraft(principal.id(), id, request);
    }

    @PostMapping("/{id}/quotation/submit")
    public QuotationResponse submitQuotation(@AuthenticationPrincipal CustomerPrincipal principal,
                                             @PathVariable @Positive Long id,
                                             @Valid @RequestBody QuotationRequest request) {
        return workflowService.submitQuotation(principal.id(), id, request);
    }

    @PostMapping("/{id}/start-repair")
    public BookingResponse startRepair(@AuthenticationPrincipal CustomerPrincipal principal,
                                       @PathVariable @Positive Long id) {
        return workflowService.startRepair(principal.id(), id);
    }

    @PostMapping("/{id}/complete-repair")
    public BookingResponse completeRepair(@AuthenticationPrincipal CustomerPrincipal principal,
                                          @PathVariable @Positive Long id) {
        return workflowService.completeRepair(principal.id(), id);
    }
}
