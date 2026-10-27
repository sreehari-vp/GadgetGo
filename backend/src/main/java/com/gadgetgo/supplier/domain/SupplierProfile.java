package com.gadgetgo.supplier.domain;

import com.gadgetgo.customer.domain.CustomerAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "supplier_profiles")
public class SupplierProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private CustomerAccount account;

    @NotBlank
    @Size(max = 160)
    @Column(name = "business_name", nullable = false, length = 160)
    private String businessName;

    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SupplierProfile() { }

    public SupplierProfile(CustomerAccount account, String businessName, String contactEmail, boolean active) {
        this.account = account;
        this.businessName = businessName.trim();
        this.contactEmail = contactEmail == null || contactEmail.isBlank() ? null : contactEmail.trim().toLowerCase(java.util.Locale.ROOT);
        this.active = active;
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
    public String getBusinessName() { return businessName; }
    public String getContactEmail() { return contactEmail; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
