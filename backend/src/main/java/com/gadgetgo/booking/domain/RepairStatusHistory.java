package com.gadgetgo.booking.domain;

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

import java.time.Instant;

@Entity
@Table(name = "repair_status_history")
public class RepairStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private RepairBooking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 24)
    private BookingStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 24, nullable = false)
    private BookingStatus toStatus;

    @Column(name = "actor_role", length = 24, nullable = false)
    private String actorRole;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RepairStatusHistory() {}

    public RepairStatusHistory(RepairBooking booking,
                               BookingStatus fromStatus,
                               BookingStatus toStatus,
                               String actorRole,
                               Long actorId,
                               String notes) {
        this.booking = booking;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorRole = actorRole;
        this.actorId = actorId;
        this.notes = notes;
    }

    @PrePersist
    void onPersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public RepairBooking getBooking() { return booking; }
    public BookingStatus getFromStatus() { return fromStatus; }
    public BookingStatus getToStatus() { return toStatus; }
    public String getActorRole() { return actorRole; }
    public Long getActorId() { return actorId; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
