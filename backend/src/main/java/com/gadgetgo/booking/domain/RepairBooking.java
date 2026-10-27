package com.gadgetgo.booking.domain;

import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.gadget.domain.CustomerGadget;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.domain.TechnicianServiceMode;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "repair_bookings")
public class RepairBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerAccount customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gadget_id", nullable = false)
    private CustomerGadget gadget;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_service_id", nullable = false)
    private RepairService repairService;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", nullable = false, length = 24)
    private TechnicianServiceMode serviceMode;

    @NotBlank
    @Column(name = "problem_description", nullable = false)
    private String problemDescription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private BookingStatus status = BookingStatus.PENDING_MATCH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    private TechnicianProfile assignedTechnician;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "repair_started_at")
    private Instant repairStartedAt;

    @Column(name = "repair_completed_at")
    private Instant repairCompletedAt;

    protected RepairBooking() { }

    public RepairBooking(CustomerAccount customer,
                         CustomerGadget gadget,
                         RepairService repairService,
                         TechnicianServiceMode serviceMode,
                         String problemDescription) {
        this.customer = customer;
        this.gadget = gadget;
        this.repairService = repairService;
        this.serviceMode = serviceMode;
        this.problemDescription = problemDescription;
        this.status = BookingStatus.PENDING_MATCH;
    }

    @PrePersist
    void setCreatedTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void setUpdatedTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public CustomerAccount getCustomer() { return customer; }
    public CustomerGadget getGadget() { return gadget; }
    public RepairService getRepairService() { return repairService; }
    public TechnicianServiceMode getServiceMode() { return serviceMode; }
    public String getProblemDescription() { return problemDescription; }
    public BookingStatus getStatus() { return status; }
    public TechnicianProfile getAssignedTechnician() { return assignedTechnician; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getRepairStartedAt() { return repairStartedAt; }
    public Instant getRepairCompletedAt() { return repairCompletedAt; }

    public void markOffered() {
        if (this.status != BookingStatus.PENDING_MATCH && this.status != BookingStatus.OFFERED && this.status != BookingStatus.NO_MATCH) {
            throw new IllegalStateException("Booking cannot transition to OFFERED from " + this.status);
        }
        this.status = BookingStatus.OFFERED;
        this.assignedTechnician = null;
    }

    public void markAssigned(TechnicianProfile technician) {
        if (this.status != BookingStatus.OFFERED && this.status != BookingStatus.PENDING_MATCH) {
            throw new IllegalStateException("Booking cannot transition to ASSIGNED from " + this.status);
        }
        if (technician == null) {
            throw new IllegalArgumentException("Assigned technician cannot be null.");
        }
        this.status = BookingStatus.ASSIGNED;
        this.assignedTechnician = technician;
    }

    public void markDiagnosed() {
        if (this.status != BookingStatus.ASSIGNED && this.status != BookingStatus.DIAGNOSED) {
            throw new IllegalStateException("Booking cannot transition to DIAGNOSED from " + this.status);
        }
        this.status = BookingStatus.DIAGNOSED;
    }

    public void markQuoted() {
        if (this.status != BookingStatus.ASSIGNED && this.status != BookingStatus.DIAGNOSED) {
            throw new IllegalStateException("Booking cannot transition to QUOTED from " + this.status);
        }
        this.status = BookingStatus.QUOTED;
    }

    public void markApproved() {
        if (this.status != BookingStatus.QUOTED) {
            throw new IllegalStateException("Booking cannot transition to APPROVED from " + this.status + ". A submitted quotation is required.");
        }
        this.status = BookingStatus.APPROVED;
    }

    public void markQuotationRejected() {
        if (this.status != BookingStatus.QUOTED) {
            throw new IllegalStateException("Booking cannot record quotation rejection from status " + this.status);
        }
        this.status = BookingStatus.ASSIGNED;
    }

    public void markRepairStarted() {
        if (this.status != BookingStatus.APPROVED) {
            throw new IllegalStateException("Repair cannot begin before customer quotation approval. Current status: " + this.status);
        }
        this.status = BookingStatus.IN_PROGRESS;
        this.repairStartedAt = Instant.now();
    }

    public void markRepairCompleted() {
        if (this.status != BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress repairs can be completed. Current status: " + this.status);
        }
        this.status = BookingStatus.COMPLETED;
        this.repairCompletedAt = Instant.now();
    }

    public void markNoMatch() {
        if (this.status != BookingStatus.PENDING_MATCH && this.status != BookingStatus.OFFERED) {
            throw new IllegalStateException("An assigned or in-progress booking cannot transition to NO_MATCH.");
        }
        this.status = BookingStatus.NO_MATCH;
        this.assignedTechnician = null;
    }

    public void markCancelled() {
        if (this.status == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed repairs cannot be cancelled.");
        }
        this.status = BookingStatus.CANCELLED;
    }
}
