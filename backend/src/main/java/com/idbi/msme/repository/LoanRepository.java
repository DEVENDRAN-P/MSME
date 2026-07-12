package com.idbi.msme.repository;

import com.idbi.msme.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByBusinessOwnerId(UUID ownerId);
    List<Loan> findByBusinessId(UUID businessId);
}
