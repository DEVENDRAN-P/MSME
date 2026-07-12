package com.idbi.msme.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aa_bank_transactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"business_id", "month"})})
public class AaBankTransaction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 7)
    private String month; // YYYY-MM

    @Column(name = "avg_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgBalance;

    @Column(name = "inward_remittances", nullable = false, precision = 15, scale = 2)
    private BigDecimal inwardRemittances;

    @Column(name = "outward_remittances", nullable = false, precision = 15, scale = 2)
    private BigDecimal outwardRemittances;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AaBankTransaction() {
    }

    public AaBankTransaction(UUID id, Business business, String month, BigDecimal avgBalance, BigDecimal inwardRemittances, BigDecimal outwardRemittances) {
        this.id = id != null ? id : UUID.randomUUID();
        this.business = business;
        this.month = month;
        this.avgBalance = avgBalance;
        this.inwardRemittances = inwardRemittances;
        this.outwardRemittances = outwardRemittances;
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getAvgBalance() {
        return avgBalance;
    }

    public void setAvgBalance(BigDecimal avgBalance) {
        this.avgBalance = avgBalance;
    }

    public BigDecimal getInwardRemittances() {
        return inwardRemittances;
    }

    public void setInwardRemittances(BigDecimal inwardRemittances) {
        this.inwardRemittances = inwardRemittances;
    }

    public BigDecimal getOutwardRemittances() {
        return outwardRemittances;
    }

    public void setOutwardRemittances(BigDecimal outwardRemittances) {
        this.outwardRemittances = outwardRemittances;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
