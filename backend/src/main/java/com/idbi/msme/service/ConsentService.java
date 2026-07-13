package com.idbi.msme.service;

import com.idbi.msme.dto.ConsentRequestDto;
import com.idbi.msme.dto.ConsentResponseDto;
import java.util.List;

public interface ConsentService {
    ConsentResponseDto requestConsent(ConsentRequestDto request, String lenderId, String lenderName, String lenderEmail);
    List<ConsentResponseDto> getMyPendingConsents(String msmeOwnerId);
    List<ConsentResponseDto> getAllMyConsents(String msmeOwnerId);
    List<ConsentResponseDto> getLenderRequestedConsents(String lenderId);
    ConsentResponseDto updateConsentStatus(String consentId, String status, String msmeOwnerId);
    boolean hasActiveConsent(String businessId, String lenderId);
}
