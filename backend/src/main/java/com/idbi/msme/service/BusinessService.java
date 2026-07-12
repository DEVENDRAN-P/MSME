package com.idbi.msme.service;

import com.idbi.msme.dto.BusinessResponse;
import com.idbi.msme.dto.RegisterBusinessRequest;

import java.util.List;
import java.util.UUID;

public interface BusinessService {
    BusinessResponse registerBusiness(RegisterBusinessRequest request, UUID ownerId);
    BusinessResponse getBusinessByOwner(UUID ownerId);
    BusinessResponse getBusinessById(UUID id);
    List<BusinessResponse> getAllBusinesses();
}

