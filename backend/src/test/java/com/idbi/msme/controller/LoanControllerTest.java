package com.idbi.msme.controller;

import com.idbi.msme.dto.ApproveLoanRequest;
import com.idbi.msme.dto.LoanResponse;
import com.idbi.msme.security.CustomUserDetailsService;
import com.idbi.msme.security.JwtAuthenticationFilter;
import com.idbi.msme.security.JwtTokenProvider;
import com.idbi.msme.client.AiServiceClient;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.service.ConsentService;
import com.idbi.msme.service.DataIngestService;
import com.idbi.msme.service.LoanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private BusinessRepository businessRepository;

    @MockBean
    private DataIngestService dataIngestService;

    @MockBean
    private AiServiceClient aiServiceClient;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testApproveLoan_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        Mockito.when(consentService.hasActiveConsent(any(), any())).thenReturn(true);

        LoanResponse response = new LoanResponse(
                UUID.randomUUID(),
                businessId,
                "Legal Name",
                BigDecimal.valueOf(1000000),
                BigDecimal.valueOf(11.5),
                12,
                "DISBURSED",
                LocalDateTime.now()
        );
        Mockito.when(loanService.approveLoan(any(ApproveLoanRequest.class))).thenReturn(response);

        mockMvc.perform(post("/loans/approve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":\"" + businessId + "\",\"amount\":1000000,\"interestRate\":11.5,\"tenureMonths\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000000))
                .andExpect(jsonPath("$.interestRate").value(11.5))
                .andExpect(jsonPath("$.status").value("DISBURSED"));
    }

    @Test
    public void testGetMyLoans_Success() throws Exception {
        Mockito.when(loanService.getMyLoans(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/loans/my-loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
