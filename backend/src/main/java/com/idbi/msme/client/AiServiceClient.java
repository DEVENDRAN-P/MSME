package com.idbi.msme.client;

import com.idbi.msme.dto.FeatureResponse;

import com.idbi.msme.dto.ForecastResponse;
import com.idbi.msme.dto.HealthCardResponse;
import com.idbi.msme.dto.IngestSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AiServiceClient(
            RestTemplate restTemplate,
            @Value("${app.ai-service.url:http://localhost:8000}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    public FeatureResponse extractFeatures(IngestSummaryResponse rawData) {
        String url = aiServiceUrl + "/api/ai/features";
        logger.info("Calling Python AI Engine for features calculation: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<IngestSummaryResponse> request = new HttpEntity<>(rawData, headers);

            FeatureResponse response = restTemplate.postForObject(url, request, FeatureResponse.class);
            if (response == null) {
                logger.error("AI service returned empty response");
                throw new RuntimeException("Empty response from AI Engine");
            }
            return response;
        } catch (Exception ex) {
            logger.error("Failed to connect to Python AI Engine: {}", ex.getMessage());
            throw new RuntimeException("AI calculation engine is currently unavailable", ex);
        }
    }

    public HealthCardResponse getCreditScore(FeatureResponse features) {
        String url = aiServiceUrl + "/api/ai/score";
        logger.info("Calling Python AI Engine for credit scoring: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FeatureResponse> request = new HttpEntity<>(features, headers);

            HealthCardResponse response = restTemplate.postForObject(url, request, HealthCardResponse.class);
            if (response == null) {
                logger.error("AI service returned empty response for credit scoring");
                throw new RuntimeException("Empty response from AI credit score engine");
            }
            return response;
        } catch (Exception ex) {
            logger.error("Failed to connect to Python AI scoring engine: {}", ex.getMessage());
            throw new RuntimeException("AI credit score engine is currently unavailable", ex);
        }
    }

    public List<ForecastResponse> getForecastProjections(Map<String, Object> payload) {
        String url = aiServiceUrl + "/api/ai/forecast";
        logger.info("Calling Python AI Engine for cash flow forecasting: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ForecastResponse[] response = restTemplate.postForObject(url, request, ForecastResponse[].class);
            if (response == null) {
                logger.error("AI service returned empty response for forecasting");
                throw new RuntimeException("Empty response from AI forecasting engine");
            }
            return Arrays.asList(response);
        } catch (Exception ex) {
            logger.error("Failed to connect to Python AI forecasting engine: {}", ex.getMessage());
            throw new RuntimeException("AI forecasting engine is currently unavailable", ex);
        }
    }
}


