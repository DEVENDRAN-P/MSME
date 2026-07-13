package com.idbi.msme.service;

import com.idbi.msme.dto.BusinessResponse;
import com.idbi.msme.dto.RegisterBusinessRequest;
import java.util.List;

public interface BusinessService {
    BusinessResponse registerBusiness(RegisterBusinessRequest request, String ownerId);
    BusinessResponse getBusinessByOwner(String ownerId);
    BusinessResponse getBusinessById(String id);
    List<BusinessResponse> getAllBusinesses();
}
