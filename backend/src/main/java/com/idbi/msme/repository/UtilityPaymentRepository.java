package com.idbi.msme.repository;

import com.idbi.msme.model.UtilityPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UtilityPaymentRepository extends JpaRepository<UtilityPayment, UUID> {
    List<UtilityPayment> findByBusinessIdOrderByBillingMonthAsc(UUID businessId);
    List<UtilityPayment> findByBusinessIdAndUtilityTypeOrderByBillingMonthAsc(UUID businessId, String utilityType);
    void deleteByBusinessId(UUID businessId);
}
