package com.idbi.msme.repository;

import com.idbi.msme.model.UpiTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UpiTransactionRepository extends JpaRepository<UpiTransaction, UUID> {
    List<UpiTransaction> findByBusinessIdOrderByMonthAsc(UUID businessId);
    void deleteByBusinessId(UUID businessId);
}
