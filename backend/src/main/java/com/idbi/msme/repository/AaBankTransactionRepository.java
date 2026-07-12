package com.idbi.msme.repository;

import com.idbi.msme.model.AaBankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AaBankTransactionRepository extends JpaRepository<AaBankTransaction, UUID> {
    List<AaBankTransaction> findByBusinessIdOrderByMonthAsc(UUID businessId);
    void deleteByBusinessId(UUID businessId);
}
