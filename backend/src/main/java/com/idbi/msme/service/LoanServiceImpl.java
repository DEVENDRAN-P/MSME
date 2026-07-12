package com.idbi.msme.service;

import com.idbi.msme.dto.ApproveLoanRequest;
import com.idbi.msme.dto.LoanResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.Business;
import com.idbi.msme.model.Loan;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BusinessRepository businessRepository;

    public LoanServiceImpl(LoanRepository loanRepository, BusinessRepository businessRepository) {
        this.loanRepository = loanRepository;
        this.businessRepository = businessRepository;
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(ApproveLoanRequest request) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found with ID: " + request.getBusinessId()));

        Loan loan = new Loan(
                UUID.randomUUID(),
                business,
                request.getAmount(),
                request.getInterestRate(),
                request.getTenureMonths(),
                "DISBURSED"
        );

        Loan saved = loanRepository.save(loan);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getMyLoans(UUID ownerId) {
        return loanRepository.findByBusinessOwnerId(ownerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getBusinessLoans(UUID businessId) {
        return loanRepository.findByBusinessId(businessId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private LoanResponse mapToResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBusiness().getId(),
                loan.getBusiness().getLegalName(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getStatus(),
                loan.getDisbursedAt()
        );
    }
}
