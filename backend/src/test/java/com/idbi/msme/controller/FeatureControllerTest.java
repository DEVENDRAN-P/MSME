package com.idbi.msme.controller;

import com.idbi.msme.dto.FeatureResponse;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeatureController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FeatureControllerTest {

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
    public void testGetMyFeatures_Success() throws Exception {
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
        mockFeatures.setGstTurnoverGrowthRate(BigDecimal.valueOf(0.12));
        mockFeatures.setGstFilingDisciplineRatio(BigDecimal.valueOf(1.0));
        mockFeatures.setUpiPenetrationRatio(BigDecimal.valueOf(0.45));
        mockFeatures.setBankCashCoverageRatio(BigDecimal.valueOf(1.05));

        Mockito.when(aiServiceClient.extractFeatures(any(IngestSummaryResponse.class))).thenReturn(mockFeatures);

        mockMvc.perform(get("/features/my-features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gst_turnover_growth_rate").value(0.12))
                .andExpect(jsonPath("$.gst_filing_discipline_ratio").value(1.0))
                .andExpect(jsonPath("$.upi_penetration_ratio").value(0.45))
                .andExpect(jsonPath("$.bank_cash_coverage_ratio").value(1.05));
    }
}
