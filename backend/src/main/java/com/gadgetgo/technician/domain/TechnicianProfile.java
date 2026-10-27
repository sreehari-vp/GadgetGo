package com.gadgetgo.technician.domain;

import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.customer.domain.CustomerAccount;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "technician_profiles")
public class TechnicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private CustomerAccount account;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "service_area", length = 160)
    private String serviceArea;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false, length = 24)
    private TechnicianAvailability availability = TechnicianAvailability.NOT_CONFIGURED;

    @Positive
    @Column(name = "capacity_limit", nullable = false)
    private int capacityLimit = 3;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "technician_service_modes", joinColumns = @JoinColumn(name = "technician_profile_id"))
    @Column(name = "service_mode", nullable = false, length = 24)
    @Enumerated(EnumType.STRING)
    private Set<TechnicianServiceMode> serviceModes = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "technician_skills",
            joinColumns = @JoinColumn(name = "technician_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "repair_service_id"))
    private Set<RepairService> skills = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TechnicianProfile() { }

    public TechnicianProfile(CustomerAccount account, boolean active, String serviceArea,
                             TechnicianAvailability availability, int capacityLimit) {
        this.account = account;
        this.active = active;
        this.serviceArea = serviceArea;
        this.availability = availability;
        this.capacityLimit = capacityLimit;
    }

    @PrePersist
    void setCreatedTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void setUpdatedTimestamp() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public CustomerAccount getAccount() { return account; }
    public boolean isActive() { return active; }
    public String getServiceArea() { return serviceArea; }
    public TechnicianAvailability getAvailability() { return availability; }
    public int getCapacityLimit() { return capacityLimit; }
    public Set<TechnicianServiceMode> getServiceModes() { return Set.copyOf(serviceModes); }
    public Set<RepairService> getSkills() { return Set.copyOf(skills); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setServiceModes(Set<TechnicianServiceMode> serviceModes) {
        this.serviceModes = new LinkedHashSet<>(serviceModes);
    }

    public void setSkills(Set<RepairService> skills) {
        this.skills = new LinkedHashSet<>(skills);
    }

    public void setCapacityLimit(int capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public void setAvailability(TechnicianAvailability availability) {
        this.availability = availability;
    }
}
