package com.idbi.msme.controller;

import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.model.Business;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.security.CustomUserDetailsService;
import com.idbi.msme.security.JwtAuthenticationFilter;
import com.idbi.msme.security.JwtTokenProvider;
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DataIngestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DataIngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataIngestService dataIngestService;

    @MockBean
    private BusinessRepository businessRepository;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testSyncData_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        
        Business mockBusiness = new Business();
        mockBusiness.setId(businessId);
        
        Mockito.when(businessRepository.findByOwnerId(any())).thenReturn(Optional.of(mockBusiness));
        
        IngestSummaryResponse response = new IngestSummaryResponse(
                businessId, true, true, false, false, false, false,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        
        Mockito.when(dataIngestService.syncAlternateData(eq(businessId), eq("ALL"))).thenReturn(response);

        mockMvc.perform(post("/data-ingest/sync?streamType=ALL")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(businessId.toString()))
                .andExpect(jsonPath("$.gstSynced").value(true))
                .andExpect(jsonPath("$.upiSynced").value(true))
                .andExpect(jsonPath("$.aaSynced").value(false));
    }

    @Test
    public void testGetSummary_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        
        Business mockBusiness = new Business();
        mockBusiness.setId(businessId);
        
        Mockito.when(businessRepository.findByOwnerId(any())).thenReturn(Optional.of(mockBusiness));
        
        IngestSummaryResponse response = new IngestSummaryResponse(
                businessId, true, true, true, true, true, true,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        
        Mockito.when(dataIngestService.getIngestSummary(eq(businessId))).thenReturn(response);

        mockMvc.perform(get("/data-ingest/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gstSynced").value(true))
                .andExpect(jsonPath("$.upiSynced").value(true))
                .andExpect(jsonPath("$.aaSynced").value(true));
    }

    @Test
    public void testGetSummary_NoBusiness() throws Exception {
        Mockito.when(businessRepository.findByOwnerId(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/data-ingest/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Please register your business profile to view alternate data."));
    }
}
