package com.idbi.msme.service;

import com.idbi.msme.dto.BusinessResponse;
import com.idbi.msme.dto.RegisterBusinessRequest;
import com.idbi.msme.exception.ConflictException;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.BusinessProfile;
import com.idbi.msme.repository.FirestoreDataAccess;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusinessServiceImpl implements BusinessService {

    private final FirestoreDataAccess db;

    public BusinessServiceImpl(FirestoreDataAccess db) {
        this.db = db;
    }

    @Override
    public BusinessResponse registerBusiness(RegisterBusinessRequest request, String ownerId) {
        if (db.existsByOwnerId(ownerId)) {
            throw new ConflictException("Business profile is already registered for this user account.");
        }
        if (db.existsByGstin(request.getGstin())) {
            throw new ConflictException("GSTIN '" + request.getGstin() + "' is already registered.");
        }
        if (db.existsByPan(request.getPan())) {
            throw new ConflictException("PAN '" + request.getPan() + "' is already registered.");
        }
        if (db.existsByUdyamNumber(request.getUdyamNumber())) {
            throw new ConflictException("Udyam '" + request.getUdyamNumber() + "' is already registered.");
        }

        BusinessProfile business = new BusinessProfile();
        business.setId(UUID.randomUUID().toString());
        business.setOwnerId(ownerId);
        business.setLegalName(request.getLegalName());
        business.setTradeName(request.getTradeName());
        business.setGstin(request.getGstin());
        business.setPan(request.getPan());
        business.setUdyamNumber(request.getUdyamNumber());
        business.setIncorporationDate(request.getIncorporationDate() != null ? request.getIncorporationDate().toString() : null);
        business.setConstitution(request.getConstitution());
        business.setIndustrySector(request.getIndustrySector());
        business.setAddressLine1(request.getAddressLine1());
        business.setAddressLine2(request.getAddressLine2());
        business.setCity(request.getCity());
        business.setState(request.getState());
        business.setPincode(request.getPincode());
        business.setCreatedAt(LocalDateTime.now().toString());
        business.setUpdatedAt(LocalDateTime.now().toString());

        db.saveBusiness(business);
        return new BusinessResponse(business);
    }

    @Override
    public BusinessResponse getBusinessByOwner(String ownerId) {
        BusinessProfile business = db.findBusinessByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("No business profile found for this owner."));
        return new BusinessResponse(business);
    }

    @Override
    public BusinessResponse getBusinessById(String id) {
        BusinessProfile business = db.findBusinessById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + id));
        return new BusinessResponse(business);
    }

    @Override
    public List<BusinessResponse> getAllBusinesses() {
        return db.findAllBusinesses().stream()
                .map(BusinessResponse::new)
                .collect(Collectors.toList());
    }
}
