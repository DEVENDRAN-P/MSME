package com.idbi.msme.repository;

import com.idbi.msme.model.GstFiling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GstFilingRepository extends JpaRepository<GstFiling, UUID> {
    List<GstFiling> findByBusinessIdOrderByFilingMonthAsc(UUID businessId);
    void deleteByBusinessId(UUID businessId);
}
