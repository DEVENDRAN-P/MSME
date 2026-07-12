package com.idbi.msme.service;

import com.idbi.msme.dto.ConsentRequestDto;
import com.idbi.msme.dto.ConsentResponseDto;
import com.idbi.msme.model.User;

import java.util.List;
import java.util.UUID;

public interface ConsentService {
    ConsentResponseDto requestConsent(ConsentRequestDto request, User lender);
    List<ConsentResponseDto> getMyPendingConsents(UUID msmeOwnerId);
    List<ConsentResponseDto> getAllMyConsents(UUID msmeOwnerId);
    List<ConsentResponseDto> getLenderRequestedConsents(UUID lenderId);
    ConsentResponseDto updateConsentStatus(UUID consentId, String status, UUID msmeOwnerId);
    boolean hasActiveConsent(UUID businessId, UUID lenderId);
}
