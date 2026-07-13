package com.idbi.msme.service;

import com.idbi.msme.dto.ConsentRequestDto;
import com.idbi.msme.dto.ConsentResponseDto;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.BusinessProfile;
import com.idbi.msme.model.ConsentDocument;
import com.idbi.msme.repository.FirestoreDataAccess;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsentServiceImpl implements ConsentService {

    private final FirestoreDataAccess db;

    public ConsentServiceImpl(FirestoreDataAccess db) {
        this.db = db;
    }

    @Override
    public ConsentResponseDto requestConsent(ConsentRequestDto request, String lenderId, String lenderName, String lenderEmail) {
        BusinessProfile business = db.findBusinessById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + request.getBusinessId()));

        ConsentDocument consent = new ConsentDocument();
        consent.setId(UUID.randomUUID().toString());
        consent.setBusinessId(request.getBusinessId());
        consent.setBusinessName(business.getLegalName());
        consent.setRequestedById(lenderId);
        consent.setRequestedByName(lenderName);
        consent.setRequestedByEmail(lenderEmail);
        consent.setConsentType(request.getConsentType());
        consent.setValidUntil(LocalDateTime.now().plusDays(30).toString());
        consent.setStatus("PENDING");
        consent.setCreatedAt(LocalDateTime.now().toString());
        consent.setUpdatedAt(LocalDateTime.now().toString());

        db.saveConsent(consent);
        return mapToDto(consent);
    }

    @Override
    public List<ConsentResponseDto> getMyPendingConsents(String msmeOwnerId) {
        return db.findConsentsByOwnerIdAndStatus(msmeOwnerId, "PENDING")
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<ConsentResponseDto> getAllMyConsents(String msmeOwnerId) {
        return db.findConsentsByOwnerId(msmeOwnerId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<ConsentResponseDto> getLenderRequestedConsents(String lenderId) {
        return db.findConsentsByLenderId(lenderId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ConsentResponseDto updateConsentStatus(String consentId, String status, String msmeOwnerId) {
        ConsentDocument consent = db.findConsentById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with ID: " + consentId));

        var business = db.findBusinessById(consent.getBusinessId());
        if (business.isEmpty() || !business.get().getOwnerId().equals(msmeOwnerId)) {
            throw new AccessDeniedException("Access denied: You do not own the business for this consent.");
        }

        consent.setStatus(status.toUpperCase());
        if ("APPROVED".equalsIgnoreCase(status)) {
            consent.setValidUntil(LocalDateTime.now().plusDays(30).toString());
        }
        consent.setUpdatedAt(LocalDateTime.now().toString());
        db.saveConsent(consent);
        return mapToDto(consent);
    }

    @Override
    public boolean hasActiveConsent(String businessId, String lenderId) {
        return db.hasActiveConsent(businessId, lenderId);
    }

    private ConsentResponseDto mapToDto(ConsentDocument c) {
        return new ConsentResponseDto(c.getId(), c.getBusinessId(), c.getBusinessName(),
                c.getRequestedById(), c.getRequestedByName(), c.getConsentType(),
                c.getValidUntil(), c.getStatus(), c.getCreatedAt());
    }
}
