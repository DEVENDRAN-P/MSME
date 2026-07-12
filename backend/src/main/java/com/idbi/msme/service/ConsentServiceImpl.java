package com.idbi.msme.service;

import com.idbi.msme.dto.ConsentRequestDto;
import com.idbi.msme.dto.ConsentResponseDto;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.Business;
import com.idbi.msme.model.Consent;
import com.idbi.msme.model.User;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.repository.ConsentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository consentRepository;
    private final BusinessRepository businessRepository;

    public ConsentServiceImpl(ConsentRepository consentRepository, BusinessRepository businessRepository) {
        this.consentRepository = consentRepository;
        this.businessRepository = businessRepository;
    }

    @Override
    @Transactional
    public ConsentResponseDto requestConsent(ConsentRequestDto request, User lender) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found with ID: " + request.getBusinessId()));

        // Create new PENDING consent request
        Consent consent = new Consent(
                UUID.randomUUID(),
                business,
                lender,
                request.getConsentType(),
                LocalDateTime.now().plusDays(30), // Standby validity, gets finalized on approval
                "PENDING"
        );

        Consent saved = consentRepository.save(consent);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentResponseDto> getMyPendingConsents(UUID msmeOwnerId) {
        return consentRepository.findByBusinessOwnerIdAndStatus(msmeOwnerId, "PENDING")
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentResponseDto> getAllMyConsents(UUID msmeOwnerId) {
        return consentRepository.findByBusinessOwnerId(msmeOwnerId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentResponseDto> getLenderRequestedConsents(UUID lenderId) {
        return consentRepository.findByRequestedById(lenderId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsentResponseDto updateConsentStatus(UUID consentId, String status, UUID msmeOwnerId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent record not found with ID: " + consentId));

        // Validate MSME ownership
        if (!consent.getBusiness().getOwner().getId().equals(msmeOwnerId)) {
            throw new AccessDeniedException("Access denied: You do not own the business associated with this consent.");
        }

        consent.setStatus(status.toUpperCase());
        if ("APPROVED".equalsIgnoreCase(status)) {
            consent.setValidUntil(LocalDateTime.now().plusDays(30)); // 30-day time-bound window
        }

        Consent saved = consentRepository.save(consent);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveConsent(UUID businessId, UUID lenderId) {
        return consentRepository.findActiveConsent(businessId, lenderId, LocalDateTime.now()).isPresent();
    }

    private ConsentResponseDto mapToDto(Consent consent) {
        return new ConsentResponseDto(
                consent.getId(),
                consent.getBusiness().getId(),
                consent.getBusiness().getLegalName(),
                consent.getRequestedBy().getId(),
                consent.getRequestedBy().getFullName(),
                consent.getConsentType(),
                consent.getValidUntil(),
                consent.getStatus(),
                consent.getCreatedAt()
        );
    }
}
