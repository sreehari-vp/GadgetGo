package com.gadgetgo.booking.service;

import com.gadgetgo.booking.api.dto.BookingResponse;
import com.gadgetgo.booking.api.dto.JobDetailResponse;
import com.gadgetgo.booking.api.dto.QuotationLineItemDto;
import com.gadgetgo.booking.api.dto.QuotationRequest;
import com.gadgetgo.booking.api.dto.QuotationResponse;
import com.gadgetgo.booking.api.dto.RepairStatusHistoryResponse;
import com.gadgetgo.booking.domain.BookingStatus;
import com.gadgetgo.booking.domain.QuotationStatus;
import com.gadgetgo.booking.domain.RepairBooking;
import com.gadgetgo.booking.domain.RepairQuotation;
import com.gadgetgo.booking.domain.RepairStatusHistory;
import com.gadgetgo.booking.repository.RepairBookingRepository;
import com.gadgetgo.booking.repository.RepairQuotationRepository;
import com.gadgetgo.booking.repository.RepairStatusHistoryRepository;
import com.gadgetgo.technician.domain.TechnicianProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RepairWorkflowService {

    private final RepairBookingRepository bookingRepository;
    private final RepairQuotationRepository quotationRepository;
    private final RepairStatusHistoryRepository historyRepository;

    public RepairWorkflowService(RepairBookingRepository bookingRepository,
                                 RepairQuotationRepository quotationRepository,
                                 RepairStatusHistoryRepository historyRepository) {
        this.bookingRepository = bookingRepository;
        this.quotationRepository = quotationRepository;
        this.historyRepository = historyRepository;
    }

    public JobDetailResponse getJobDetailForTechnician(Long technicianAccountId, Long bookingId) {
        RepairBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(BookingNotFoundException::new);

        validateTechnicianAssignment(booking, technicianAccountId);

        List<RepairQuotation> allQuotes = quotationRepository.findAllByBooking_IdOrderByVersionDesc(bookingId);
        RepairQuotation currentQuote = allQuotes.isEmpty() ? null : allQuotes.get(0);
        List<RepairStatusHistory> history = historyRepository.findAllByBooking_IdOrderByCreatedAtAsc(bookingId);

        return new JobDetailResponse(
                BookingResponse.from(booking),
                QuotationResponse.from(currentQuote),
                allQuotes.stream().map(QuotationResponse::from).toList(),
                history.stream().map(RepairStatusHistoryResponse::from).toList()
        );
    }

    @Transactional
    public QuotationResponse saveQuotationDraft(Long technicianAccountId, Long bookingId, QuotationRequest request) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .orElseThrow(BookingNotFoundException::new);

        validateTechnicianAssignment(booking, technicianAccountId);

        if (booking.getStatus() != BookingStatus.ASSIGNED && booking.getStatus() != BookingStatus.DIAGNOSED) {
            throw new InvalidRepairStateException("Cannot edit diagnosis or draft quotation when booking status is " + booking.getStatus());
        }

        TechnicianProfile technician = booking.getAssignedTechnician();
        List<RepairQuotation> existingQuotes = quotationRepository.findAllByBooking_IdOrderByVersionDesc(bookingId);

        RepairQuotation draft = existingQuotes.stream()
                .filter(q -> q.getStatus() == QuotationStatus.DRAFT)
                .findFirst()
                .orElse(null);

        if (draft == null) {
            int nextVersion = existingQuotes.isEmpty() ? 1 : existingQuotes.get(0).getVersion() + 1;
            draft = new RepairQuotation(
                    booking,
                    technician,
                    nextVersion,
                    request.diagnosedFault(),
                    request.proposedRepair(),
                    request.estimatedDuration(),
                    request.labourCharge(),
                    request.additionalCharge()
            );
        } else {
            draft.updateDetails(
                    request.diagnosedFault(),
                    request.proposedRepair(),
                    request.estimatedDuration(),
                    request.labourCharge(),
                    request.additionalCharge()
            );
            draft.clearLineItems();
        }

        populateLineItems(draft, request.lineItems());
        RepairQuotation saved = quotationRepository.save(draft);

        if (booking.getStatus() == BookingStatus.ASSIGNED) {
            booking.markDiagnosed();
            historyRepository.save(new RepairStatusHistory(
                    booking,
                    BookingStatus.ASSIGNED,
                    BookingStatus.DIAGNOSED,
                    "TECHNICIAN",
                    technicianAccountId,
                    "Diagnosis and draft quotation recorded by technician."
            ));
        }

        return QuotationResponse.from(saved);
    }

    @Transactional
    public QuotationResponse submitQuotation(Long technicianAccountId, Long bookingId, QuotationRequest request) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .orElseThrow(BookingNotFoundException::new);

        validateTechnicianAssignment(booking, technicianAccountId);

        if (booking.getStatus() != BookingStatus.ASSIGNED && booking.getStatus() != BookingStatus.DIAGNOSED) {
            throw new InvalidRepairStateException("Quotation cannot be submitted when booking status is " + booking.getStatus());
        }

        TechnicianProfile technician = booking.getAssignedTechnician();
        List<RepairQuotation> existingQuotes = quotationRepository.findAllByBooking_IdOrderByVersionDesc(bookingId);

        RepairQuotation quote = existingQuotes.stream()
                .filter(q -> q.getStatus() == QuotationStatus.DRAFT)
                .findFirst()
                .orElse(null);

        if (quote == null) {
            int nextVersion = existingQuotes.isEmpty() ? 1 : existingQuotes.get(0).getVersion() + 1;
            quote = new RepairQuotation(
                    booking,
                    technician,
                    nextVersion,
                    request.diagnosedFault(),
                    request.proposedRepair(),
                    request.estimatedDuration(),
                    request.labourCharge(),
                    request.additionalCharge()
            );
        } else {
            quote.updateDetails(
                    request.diagnosedFault(),
                    request.proposedRepair(),
                    request.estimatedDuration(),
                    request.labourCharge(),
                    request.additionalCharge()
            );
            quote.clearLineItems();
        }

        populateLineItems(quote, request.lineItems());
        quote.submit();

        RepairQuotation saved = quotationRepository.save(quote);
        BookingStatus prevStatus = booking.getStatus();
        booking.markQuoted();

        historyRepository.save(new RepairStatusHistory(
                booking,
                prevStatus,
                BookingStatus.QUOTED,
                "TECHNICIAN",
                technicianAccountId,
                "Quotation v" + saved.getVersion() + " submitted to customer for review. Total: " + saved.getTotalAmount()
        ));

        return QuotationResponse.from(saved);
    }

    @Transactional
    public BookingResponse startRepair(Long technicianAccountId, Long bookingId) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .orElseThrow(BookingNotFoundException::new);

        validateTechnicianAssignment(booking, technicianAccountId);

        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new InvalidRepairStateException("Repair is already in progress.");
        }

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new InvalidRepairStateException("Repair cannot begin before explicit customer quotation approval. Current status: " + booking.getStatus());
        }

        Optional<RepairQuotation> approvedQuote = quotationRepository.findFirstByBooking_IdAndStatusOrderByVersionDesc(bookingId, QuotationStatus.APPROVED);
        if (approvedQuote.isEmpty()) {
            throw new InvalidRepairStateException("No approved quotation found. Repair cannot begin without customer approval.");
        }

        booking.markRepairStarted();

        historyRepository.save(new RepairStatusHistory(
                booking,
                BookingStatus.APPROVED,
                BookingStatus.IN_PROGRESS,
                "TECHNICIAN",
                technicianAccountId,
                "Repair execution started by technician."
        ));

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse completeRepair(Long technicianAccountId, Long bookingId) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .orElseThrow(BookingNotFoundException::new);

        validateTechnicianAssignment(booking, technicianAccountId);

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new InvalidRepairStateException("Only in-progress repairs can be marked completed. Current status: " + booking.getStatus());
        }

        booking.markRepairCompleted();

        historyRepository.save(new RepairStatusHistory(
                booking,
                BookingStatus.IN_PROGRESS,
                BookingStatus.COMPLETED,
                "TECHNICIAN",
                technicianAccountId,
                "Repair completed successfully."
        ));

        return BookingResponse.from(booking);
    }

    public JobDetailResponse getJobDetailForCustomer(Long customerId, Long bookingId) {
        RepairBooking booking = bookingRepository.findByIdAndCustomer_Id(bookingId, customerId)
                .orElseThrow(BookingNotFoundException::new);

        List<RepairQuotation> allQuotes = quotationRepository.findAllByBooking_IdOrderByVersionDesc(bookingId).stream()
                .filter(q -> q.getStatus() != QuotationStatus.DRAFT)
                .toList();

        RepairQuotation currentQuote = allQuotes.isEmpty() ? null : allQuotes.get(0);
        List<RepairStatusHistory> history = historyRepository.findAllByBooking_IdOrderByCreatedAtAsc(bookingId);

        return new JobDetailResponse(
                BookingResponse.from(booking),
                QuotationResponse.from(currentQuote),
                allQuotes.stream().map(QuotationResponse::from).toList(),
                history.stream().map(RepairStatusHistoryResponse::from).toList()
        );
    }

    @Transactional
    public QuotationResponse approveQuotation(Long customerId, Long bookingId) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .filter(b -> b.getCustomer().getId().equals(customerId))
                .orElseThrow(BookingNotFoundException::new);

        if (booking.getStatus() != BookingStatus.QUOTED) {
            throw new InvalidRepairStateException("Quotation cannot be approved when booking status is " + booking.getStatus());
        }

        RepairQuotation quotation = quotationRepository.findFirstByBooking_IdOrderByVersionDesc(bookingId)
                .filter(q -> q.getStatus() == QuotationStatus.SUBMITTED)
                .orElseThrow(() -> new InvalidRepairStateException("No submitted quotation awaiting approval for this booking."));

        quotation.approve();
        booking.markApproved();

        historyRepository.save(new RepairStatusHistory(
                booking,
                BookingStatus.QUOTED,
                BookingStatus.APPROVED,
                "CUSTOMER",
                customerId,
                "Quotation v" + quotation.getVersion() + " approved by customer for amount: " + quotation.getTotalAmount()
        ));

        return QuotationResponse.from(quotation);
    }

    @Transactional
    public QuotationResponse rejectQuotation(Long customerId, Long bookingId, String notes) {
        RepairBooking booking = bookingRepository.findWithLockById(bookingId)
                .filter(b -> b.getCustomer().getId().equals(customerId))
                .orElseThrow(BookingNotFoundException::new);

        if (booking.getStatus() != BookingStatus.QUOTED) {
            throw new InvalidRepairStateException("Quotation cannot be rejected when booking status is " + booking.getStatus());
        }

        RepairQuotation quotation = quotationRepository.findFirstByBooking_IdOrderByVersionDesc(bookingId)
                .filter(q -> q.getStatus() == QuotationStatus.SUBMITTED)
                .orElseThrow(() -> new InvalidRepairStateException("No submitted quotation awaiting decision for this booking."));

        quotation.reject(notes);
        booking.markQuotationRejected();

        historyRepository.save(new RepairStatusHistory(
                booking,
                BookingStatus.QUOTED,
                BookingStatus.ASSIGNED,
                "CUSTOMER",
                customerId,
                "Quotation v" + quotation.getVersion() + " rejected by customer." + (notes != null ? " Reason: " + notes : "")
        ));

        return QuotationResponse.from(quotation);
    }

    private void validateTechnicianAssignment(RepairBooking booking, Long technicianAccountId) {
        if (booking.getAssignedTechnician() == null ||
                booking.getAssignedTechnician().getAccount() == null ||
                !booking.getAssignedTechnician().getAccount().getId().equals(technicianAccountId)) {
            throw new UnauthorizedJobAccessException("You are not the technician assigned to repair booking #" + booking.getId());
        }
    }

    private void populateLineItems(RepairQuotation quotation, List<QuotationLineItemDto> items) {
        if (items == null || items.isEmpty()) return;
        for (QuotationLineItemDto item : items) {
            if (item.partName() == null || item.partName().isBlank()) continue;
            BigDecimal price = item.unitPrice() != null ? item.unitPrice() : BigDecimal.ZERO;
            int qty = Math.max(1, item.quantity());
            quotation.addLineItem(item.partName(), qty, price.setScale(2, RoundingMode.HALF_UP));
        }
    }
}
