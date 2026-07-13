package com.idbi.msme.service;

import com.idbi.msme.dto.ApproveLoanRequest;
import com.idbi.msme.dto.LoanResponse;
import java.util.List;

public interface LoanService {
    LoanResponse approveLoan(ApproveLoanRequest request);
    List<LoanResponse> getMyLoans(String ownerId);
    List<LoanResponse> getBusinessLoans(String businessId);
}
