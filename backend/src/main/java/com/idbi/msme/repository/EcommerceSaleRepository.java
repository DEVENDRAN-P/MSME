package com.idbi.msme.repository;

import com.idbi.msme.model.EcommerceSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EcommerceSaleRepository extends JpaRepository<EcommerceSale, UUID> {
    List<EcommerceSale> findByBusinessIdOrderByMonthAsc(UUID businessId);
    List<EcommerceSale> findByBusinessIdAndPlatformOrderByMonthAsc(UUID businessId, String platform);
    void deleteByBusinessId(UUID businessId);
}
