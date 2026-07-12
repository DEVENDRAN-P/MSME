package com.idbi.msme.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ecommerce_sales", uniqueConstraints = {@UniqueConstraint(columnNames = {"business_id", "platform", "month"})})
public class EcommerceSale {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 50)
    private String platform; // AMAZON, FLIPKART, ONDC

    @Column(nullable = false, length = 7)
    private String month; // YYYY-MM

    @Column(name = "sales_volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal salesVolume;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public EcommerceSale() {
    }

    public EcommerceSale(UUID id, Business business, String platform, String month, BigDecimal salesVolume, Integer orderCount) {
        this.id = id != null ? id : UUID.randomUUID();
        this.business = business;
        this.platform = platform;
        this.month = month;
        this.salesVolume = salesVolume;
        this.orderCount = orderCount;
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(BigDecimal salesVolume) {
        this.salesVolume = salesVolume;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
