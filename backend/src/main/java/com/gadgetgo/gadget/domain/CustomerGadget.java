package com.gadgetgo.gadget.domain;

import com.gadgetgo.catalog.domain.DeviceCategory;
import com.gadgetgo.customer.domain.CustomerAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customer_gadgets")
public class CustomerGadget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerAccount customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private DeviceCategory category;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String brand;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String model;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerGadget() {
    }

    public CustomerGadget(CustomerAccount customer, DeviceCategory category, String brand, String model,
                          LocalDate purchaseDate, LocalDate warrantyExpiryDate) {
        this.customer = customer;
        this.category = category;
        updateDetails(category, brand, model, purchaseDate, warrantyExpiryDate);
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
    public CustomerAccount getCustomer() { return customer; }
    public DeviceCategory getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateDetails(DeviceCategory category, String brand, String model,
                              LocalDate purchaseDate, LocalDate warrantyExpiryDate) {
        this.category = category;
        this.brand = brand.trim();
        this.model = model.trim();
        this.purchaseDate = purchaseDate;
        this.warrantyExpiryDate = warrantyExpiryDate;
    }
}
