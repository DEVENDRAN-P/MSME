package com.idbi.msme.repository;

import com.idbi.msme.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {
    Optional<Business> findByOwnerId(UUID ownerId);
    boolean existsByOwnerId(UUID ownerId);
    boolean existsByGstin(String gstin);
    boolean existsByPan(String pan);
    boolean existsByUdyamNumber(String udyamNumber);
}
