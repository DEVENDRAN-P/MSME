package com.idbi.msme.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "upi_transactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"business_id", "month"})})
public class UpiTransaction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 7)
    private String month; // YYYY-MM

    @Column(name = "total_credit_volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCreditVolume;

    @Column(name = "total_credit_count", nullable = false)
    private Integer totalCreditCount;

    @Column(name = "total_debit_volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDebitVolume;

    @Column(name = "total_debit_count", nullable = false)
    private Integer totalDebitCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UpiTransaction() {
    }

    public UpiTransaction(UUID id, Business business, String month, BigDecimal totalCreditVolume, Integer totalCreditCount, BigDecimal totalDebitVolume, Integer totalDebitCount) {
        this.id = id != null ? id : UUID.randomUUID();
        this.business = business;
        this.month = month;
        this.totalCreditVolume = totalCreditVolume;
        this.totalCreditCount = totalCreditCount;
        this.totalDebitVolume = totalDebitVolume;
        this.totalDebitCount = totalDebitCount;
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

    public BigDecimal getTotalCreditVolume() {
        return totalCreditVolume;
    }

    public void setTotalCreditVolume(BigDecimal totalCreditVolume) {
        this.totalCreditVolume = totalCreditVolume;
    }

    public Integer getTotalCreditCount() {
        return totalCreditCount;
    }

    public void setTotalCreditCount(Integer totalCreditCount) {
        this.totalCreditCount = totalCreditCount;
    }

    public BigDecimal getTotalDebitVolume() {
        return totalDebitVolume;
    }

    public void setTotalDebitVolume(BigDecimal totalDebitVolume) {
        this.totalDebitVolume = totalDebitVolume;
    }

    public Integer getTotalDebitCount() {
        return totalDebitCount;
    }

    public void setTotalDebitCount(Integer totalDebitCount) {
        this.totalDebitCount = totalDebitCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
