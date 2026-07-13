package com.idbi.msme.service;

import com.idbi.msme.dto.IngestSummaryResponse;

public interface DataIngestService {
    IngestSummaryResponse getIngestSummary(String businessId);
    IngestSummaryResponse syncAlternateData(String businessId, String streamType);
}
