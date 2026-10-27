package com.gadgetgo.booking.domain;

import com.gadgetgo.technician.domain.TechnicianProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_quotations")
public class RepairQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private RepairBooking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_profile_id", nullable = false)
    private TechnicianProfile technician;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 24, nullable = false)
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(name = "diagnosed_fault", nullable = false, columnDefinition = "TEXT")
    private String diagnosedFault;

    @Column(name = "proposed_repair", nullable = false, columnDefinition = "TEXT")
    private String proposedRepair;

    @Column(name = "estimated_duration", length = 100, nullable = false)
    private String estimatedDuration;

    @Column(name = "labour_charge", precision = 10, scale = 2, nullable = false)
    private BigDecimal labourCharge = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "additional_charge", precision = 10, scale = 2, nullable = false)
    private BigDecimal additionalCharge = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "parts_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal partsTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationLineItem> lineItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RepairQuotation() {}

    public RepairQuotation(RepairBooking booking,
                           TechnicianProfile technician,
                           int version,
                           String diagnosedFault,
                           String proposedRepair,
                           String estimatedDuration,
                           BigDecimal labourCharge,
                           BigDecimal additionalCharge) {
        if (booking == null) throw new IllegalArgumentException("Booking cannot be null.");
        if (technician == null) throw new IllegalArgumentException("Technician cannot be null.");
        if (diagnosedFault == null || diagnosedFault.isBlank()) throw new IllegalArgumentException("Diagnosed fault cannot be blank.");
        if (proposedRepair == null || proposedRepair.isBlank()) throw new IllegalArgumentException("Proposed repair cannot be blank.");
        if (estimatedDuration == null || estimatedDuration.isBlank()) throw new IllegalArgumentException("Estimated duration cannot be blank.");

        this.booking = booking;
        this.technician = technician;
        this.version = Math.max(1, version);
        this.status = QuotationStatus.DRAFT;
        this.diagnosedFault = diagnosedFault.trim();
        this.proposedRepair = proposedRepair.trim();
        this.estimatedDuration = estimatedDuration.trim();
        this.labourCharge = (labourCharge != null ? labourCharge : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.additionalCharge = (additionalCharge != null ? additionalCharge : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        recalculateTotals();
    }

    @PrePersist
    void onPersist() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addLineItem(String partName, int quantity, BigDecimal unitPrice) {
        QuotationLineItem item = new QuotationLineItem(this, partName, quantity, unitPrice);
        this.lineItems.add(item);
        recalculateTotals();
    }

    public void clearLineItems() {
        this.lineItems.clear();
        recalculateTotals();
    }

    public void updateDetails(String diagnosedFault,
                              String proposedRepair,
                              String estimatedDuration,
                              BigDecimal labourCharge,
                              BigDecimal additionalCharge) {
        if (this.status != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Only draft quotations can be modified.");
        }
        if (diagnosedFault != null && !diagnosedFault.isBlank()) this.diagnosedFault = diagnosedFault.trim();
        if (proposedRepair != null && !proposedRepair.isBlank()) this.proposedRepair = proposedRepair.trim();
        if (estimatedDuration != null && !estimatedDuration.isBlank()) this.estimatedDuration = estimatedDuration.trim();
        if (labourCharge != null) this.labourCharge = labourCharge.setScale(2, RoundingMode.HALF_UP);
        if (additionalCharge != null) this.additionalCharge = additionalCharge.setScale(2, RoundingMode.HALF_UP);
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal sumParts = BigDecimal.ZERO;
        for (QuotationLineItem item : lineItems) {
            sumParts = sumParts.add(item.getTotalPrice());
        }
        this.partsTotal = sumParts.setScale(2, RoundingMode.HALF_UP);
        this.totalAmount = this.partsTotal
                .add(this.labourCharge != null ? this.labourCharge : BigDecimal.ZERO)
                .add(this.additionalCharge != null ? this.additionalCharge : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void submit() {
        if (this.status != QuotationStatus.DRAFT) {
            throw new IllegalStateException("Only a draft quotation can be submitted.");
        }
        recalculateTotals();
        this.status = QuotationStatus.SUBMITTED;
        this.submittedAt = Instant.now();
    }

    public void approve() {
        if (this.status != QuotationStatus.SUBMITTED) {
            throw new IllegalStateException("Only a submitted quotation can be approved.");
        }
        this.status = QuotationStatus.APPROVED;
        this.respondedAt = Instant.now();
    }

    public void reject(String notes) {
        if (this.status != QuotationStatus.SUBMITTED) {
            throw new IllegalStateException("Only a submitted quotation can be rejected.");
        }
        this.status = QuotationStatus.REJECTED;
        this.customerNotes = (notes != null && !notes.isBlank()) ? notes.trim() : null;
        this.respondedAt = Instant.now();
    }

    public void supersede() {
        if (this.status == QuotationStatus.APPROVED) {
            throw new IllegalStateException("An approved quotation cannot be superseded.");
        }
        this.status = QuotationStatus.SUPERSEDED;
    }

    public Long getId() { return id; }
    public RepairBooking getBooking() { return booking; }
    public TechnicianProfile getTechnician() { return technician; }
    public int getVersion() { return version; }
    public QuotationStatus getStatus() { return status; }
    public String getDiagnosedFault() { return diagnosedFault; }
    public String getProposedRepair() { return proposedRepair; }
    public String getEstimatedDuration() { return estimatedDuration; }
    public BigDecimal getLabourCharge() { return labourCharge; }
    public BigDecimal getAdditionalCharge() { return additionalCharge; }
    public BigDecimal getPartsTotal() { return partsTotal; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCustomerNotes() { return customerNotes; }
    public List<QuotationLineItem> getLineItems() { return lineItems; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getRespondedAt() { return respondedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
