package com.idbi.msme.controller;

import com.idbi.msme.dto.FeatureResponse;
import com.idbi.msme.dto.HealthCardResponse;
import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.model.Business;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.security.CustomUserDetailsService;
import com.idbi.msme.security.JwtAuthenticationFilter;
import com.idbi.msme.security.JwtTokenProvider;
import com.idbi.msme.client.AiServiceClient;
import com.idbi.msme.service.ConsentService;
import com.idbi.msme.service.DataIngestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthCardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HealthCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataIngestService dataIngestService;

    @MockBean
    private BusinessRepository businessRepository;

    @MockBean
    private AiServiceClient aiServiceClient;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testGetMyHealthCard_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Business mockBusiness = new Business();
        mockBusiness.setId(businessId);

        Mockito.when(businessRepository.findByOwnerId(any())).thenReturn(Optional.of(mockBusiness));

        IngestSummaryResponse rawData = new IngestSummaryResponse(
                businessId, true, true, false, false, false, false,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Mockito.when(dataIngestService.getIngestSummary(eq(businessId))).thenReturn(rawData);

        FeatureResponse mockFeatures = new FeatureResponse();
        Mockito.when(aiServiceClient.extractFeatures(any())).thenReturn(mockFeatures);

        HealthCardResponse mockCard = new HealthCardResponse();
        mockCard.setUnifiedScore(780);
        mockCard.setGrade("PRIME_PLUS");
        mockCard.setDescription("Excellent credit profile.");
        
        HealthCardResponse.DimensionScores scores = new HealthCardResponse.DimensionScores();
        scores.setRevenueHealth(85);
        scores.setComplianceHealth(100);
        scores.setLiquidityHealth(90);
        scores.setWorkforceHealth(80);
        mockCard.setDimensionScores(scores);

        Mockito.when(aiServiceClient.getCreditScore(any())).thenReturn(mockCard);

        mockMvc.perform(get("/health-card/my-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unified_score").value(780))
                .andExpect(jsonPath("$.grade").value("PRIME_PLUS"))
                .andExpect(jsonPath("$.dimension_scores.revenue_health").value(85))
                .andExpect(jsonPath("$.dimension_scores.compliance_health").value(100));
    }
}
