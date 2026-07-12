package com.idbi.msme.service;

import com.idbi.msme.dto.IngestSummaryResponse;

import java.util.UUID;

public interface DataIngestService {
    IngestSummaryResponse getIngestSummary(UUID businessId);
    IngestSummaryResponse syncAlternateData(UUID businessId, String streamType);
}
