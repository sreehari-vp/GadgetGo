package com.gadgetgo.booking.domain;

import com.gadgetgo.technician.domain.TechnicianProfile;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "assignment_offers")
public class AssignmentOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private RepairBooking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_profile_id", nullable = false)
    private TechnicianProfile technician;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private OfferStatus status = OfferStatus.OFFERED;

    @Column(name = "match_score")
    private Double matchScore;

    @Column(name = "match_rationale")
    private String matchRationale;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    protected AssignmentOffer() { }

    public AssignmentOffer(RepairBooking booking, TechnicianProfile technician) {
        this(booking, technician, null, null);
    }

    public AssignmentOffer(RepairBooking booking, TechnicianProfile technician, Double matchScore, String matchRationale) {
        this.booking = booking;
        this.technician = technician;
        this.matchScore = matchScore;
        this.matchRationale = matchRationale;
        this.status = OfferStatus.OFFERED;
    }

    @PrePersist
    void setCreatedTimestamps() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public RepairBooking getBooking() { return booking; }
    public TechnicianProfile getTechnician() { return technician; }
    public OfferStatus getStatus() { return status; }
    public Double getMatchScore() { return matchScore; }
    public String getMatchRationale() { return matchRationale; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }

    public void accept() {
        if (this.status != OfferStatus.OFFERED) {
            throw new IllegalStateException("Offer cannot be accepted because its status is " + this.status);
        }
        this.status = OfferStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    public void reject() {
        if (this.status != OfferStatus.OFFERED) {
            throw new IllegalStateException("Offer cannot be rejected because its status is " + this.status);
        }
        this.status = OfferStatus.REJECTED;
        this.respondedAt = Instant.now();
    }
}
