package com.idbi.msme.repository;

import com.idbi.msme.model.EpfoRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EpfoRecordRepository extends JpaRepository<EpfoRecord, UUID> {
    List<EpfoRecord> findByBusinessIdOrderByMonthAsc(UUID businessId);
    void deleteByBusinessId(UUID businessId);
}
