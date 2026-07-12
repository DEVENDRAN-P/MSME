package com.idbi.msme.service;

import com.idbi.msme.dto.BusinessResponse;
import com.idbi.msme.dto.RegisterBusinessRequest;
import com.idbi.msme.exception.ConflictException;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.Business;
import com.idbi.msme.model.User;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public BusinessServiceImpl(BusinessRepository businessRepository, UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BusinessResponse registerBusiness(RegisterBusinessRequest request, UUID ownerId) {
        // Validate owner existence
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner user profile not found with ID: " + ownerId));

        // Enforce one business profile per owner node
        if (businessRepository.existsByOwnerId(ownerId)) {
            throw new ConflictException("Business profile is already registered for this user account.");
        }

        // Verify tax and registration ID uniqueness
        if (businessRepository.existsByGstin(request.getGstin())) {
            throw new ConflictException("GSTIN '" + request.getGstin() + "' is already registered in our system.");
        }

        if (businessRepository.existsByPan(request.getPan())) {
            throw new ConflictException("PAN '" + request.getPan() + "' is already registered in our system.");
        }

        if (businessRepository.existsByUdyamNumber(request.getUdyamNumber())) {
            throw new ConflictException("Udyam Registration Number '" + request.getUdyamNumber() + "' is already registered.");
        }

        Business business = new Business(
                UUID.randomUUID(),
                owner,
                request.getLegalName(),
                request.getTradeName(),
                request.getGstin(),
                request.getPan(),
                request.getUdyamNumber(),
                request.getIncorporationDate(),
                request.getConstitution(),
                request.getIndustrySector(),
                request.getAddressLine1(),
                request.getAddressLine2(),
                request.getCity(),
                request.getState(),
                request.getPincode()
        );

        Business savedBusiness = businessRepository.save(business);
        return mapToResponse(savedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessByOwner(UUID ownerId) {
        Business business = businessRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("No business profile found for this user owner."));
        return mapToResponse(business);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found with ID: " + id));
        return mapToResponse(business);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<BusinessResponse> getAllBusinesses() {
        return businessRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }


    private BusinessResponse mapToResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getOwner().getId(),
                business.getLegalName(),
                business.getTradeName(),
                business.getGstin(),
                business.getPan(),
                business.getUdyamNumber(),
                business.getIncorporationDate(),
                business.getConstitution(),
                business.getIndustrySector(),
                business.getAddressLine1(),
                business.getAddressLine2(),
                business.getCity(),
                business.getState(),
                business.getPincode(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
}
