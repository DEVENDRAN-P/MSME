package com.idbi.msme.service;

import com.idbi.msme.dto.ApproveLoanRequest;
import com.idbi.msme.dto.LoanResponse;

import java.util.List;
import java.util.UUID;

public interface LoanService {
    LoanResponse approveLoan(ApproveLoanRequest request);
    List<LoanResponse> getMyLoans(UUID ownerId);
    List<LoanResponse> getBusinessLoans(UUID businessId);
}
