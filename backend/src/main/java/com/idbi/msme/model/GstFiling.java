package com.idbi.msme.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gst_filings", uniqueConstraints = {@UniqueConstraint(columnNames = {"business_id", "filing_month"})})
public class GstFiling {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "filing_month", nullable = false, length = 7)
    private String filingMonth; // YYYY-MM

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal turnover;

    @Column(name = "tax_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxPaid;

    @Column(name = "filing_status", nullable = false)
    private String filingStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GstFiling() {
    }

    public GstFiling(UUID id, Business business, String filingMonth, BigDecimal turnover, BigDecimal taxPaid, String filingStatus) {
        this.id = id != null ? id : UUID.randomUUID();
        this.business = business;
        this.filingMonth = filingMonth;
        this.turnover = turnover;
        this.taxPaid = taxPaid;
        this.filingStatus = filingStatus;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public String getFilingMonth() {
        return filingMonth;
    }

    public void setFilingMonth(String filingMonth) {
        this.filingMonth = filingMonth;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public BigDecimal getTaxPaid() {
        return taxPaid;
    }

    public void setTaxPaid(BigDecimal taxPaid) {
        this.taxPaid = taxPaid;
    }

    public String getFilingStatus() {
        return filingStatus;
    }

    public void setFilingStatus(String filingStatus) {
        this.filingStatus = filingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
