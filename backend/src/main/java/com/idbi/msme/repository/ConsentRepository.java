package com.idbi.msme.repository;

import com.idbi.msme.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    List<Consent> findByBusinessOwnerId(UUID ownerId);

    List<Consent> findByBusinessOwnerIdAndStatus(UUID ownerId, String status);

    List<Consent> findByRequestedById(UUID requestedById);

    @Query("SELECT c FROM Consent c WHERE c.business.id = :businessId " +
           "AND c.requestedBy.id = :lenderId AND c.status = 'APPROVED' AND c.validUntil > :now")
    Optional<Consent> findActiveConsent(
            @Param("businessId") UUID businessId,
            @Param("lenderId") UUID lenderId,
            @Param("now") LocalDateTime now);
}
