package com.idbi.msme.service;

import com.idbi.msme.dto.ApproveLoanRequest;
import com.idbi.msme.dto.LoanResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.BusinessProfile;
import com.idbi.msme.model.LoanDocument;
import com.idbi.msme.repository.FirestoreDataAccess;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {

    private final FirestoreDataAccess db;

    public LoanServiceImpl(FirestoreDataAccess db) {
        this.db = db;
    }

    @Override
    public LoanResponse approveLoan(ApproveLoanRequest request) {
        BusinessProfile business = db.findBusinessById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + request.getBusinessId()));

        LoanDocument loan = new LoanDocument();
        loan.setId(UUID.randomUUID().toString());
        loan.setBusinessId(request.getBusinessId());
        loan.setBusinessName(business.getLegalName());
        loan.setAmount(request.getAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTenureMonths(request.getTenureMonths());
        loan.setStatus("DISBURSED");
        loan.setDisbursedAt(LocalDateTime.now().toString());
        loan.setCreatedAt(LocalDateTime.now().toString());
        loan.setUpdatedAt(LocalDateTime.now().toString());

        db.saveLoan(loan);
        return mapToResponse(loan);
    }

    @Override
    public List<LoanResponse> getMyLoans(String ownerId) {
        return db.findLoansByOwnerId(ownerId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getBusinessLoans(String businessId) {
        return db.findLoansByBusinessId(businessId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private LoanResponse mapToResponse(LoanDocument l) {
        return new LoanResponse(l.getId(), l.getBusinessId(), l.getBusinessName(),
                l.getAmount(), l.getInterestRate(), l.getTenureMonths(),
                l.getStatus(), l.getDisbursedAt());
    }
}
