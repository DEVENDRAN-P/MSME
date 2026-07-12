package com.idbi.msme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idbi.msme.dto.BusinessResponse;
import com.idbi.msme.dto.RegisterBusinessRequest;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.security.CustomUserDetailsService;
import com.idbi.msme.security.JwtAuthenticationFilter;
import com.idbi.msme.security.JwtTokenProvider;
import com.idbi.msme.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BusinessController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessService businessService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testRegisterBusiness_Success() throws Exception {
        RegisterBusinessRequest request = new RegisterBusinessRequest(
                "Saraswati Fabrics Private Limited",
                "Saraswati Fabrics",
                "22AAAAA1111A1Z1", // Valid GST format
                "AAAAA1111A",      // Valid PAN format
                "UDYAM-MH-12-0004561",
                LocalDate.of(2018, 5, 20),
                "Private Limited Company",
                "Textiles manufacturing",
                "Plot No 45, MIDC Industrial Area",
                "Behind SBI Office",
                "Nagpur",
                "Maharashtra",
                "440012"
        );

        UUID businessId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        BusinessResponse response = new BusinessResponse(
                businessId,
                ownerId,
                request.getLegalName(),
                request.getTradeName(),
                request.getGstin(),
                request.getPan(),
                request.getUdyamNumber(),
                request.getIncorporationDate(),
                request.getConstitution(),
                request.getIndustrySector(),
                request.getAddressLine1(),
                request.getAddressLine2(),
                request.getCity(),
                request.getState(),
                request.getPincode(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Mockito.when(businessService.registerBusiness(any(RegisterBusinessRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/business/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.legalName").value("Saraswati Fabrics Private Limited"))
                .andExpect(jsonPath("$.gstin").value("22AAAAA1111A1Z1"))
                .andExpect(jsonPath("$.city").value("Nagpur"));
    }

    @Test
    public void testRegisterBusiness_ValidationFailure() throws Exception {
        RegisterBusinessRequest request = new RegisterBusinessRequest(
                "", // Blank
                "Saraswati Fabrics",
                "INVALID-GST", // Regex mismatch
                "INVALID-PAN", // Regex mismatch
                "", // Blank
                null, // Null date
                "Private Limited Company",
                "Textiles",
                "", // Blank
                "Behind SBI Office",
                "Nagpur",
                "Maharashtra",
                "4400" // Size mismatch (6 expected)
        );

        mockMvc.perform(post("/business/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.legalName").exists())
                .andExpect(jsonPath("$.errors.gstin").exists())
                .andExpect(jsonPath("$.errors.pan").exists())
                .andExpect(jsonPath("$.errors.udyamNumber").exists())
                .andExpect(jsonPath("$.errors.incorporationDate").exists())
                .andExpect(jsonPath("$.errors.pincode").exists());
    }

    @Test
    public void testGetMyBusiness_Success() throws Exception {
        UUID businessId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        BusinessResponse response = new BusinessResponse(
                businessId,
                ownerId,
                "Saraswati Fabrics Private Limited",
                "Saraswati Fabrics",
                "22AAAAA1111A1Z1",
                "AAAAA1111A",
                "UDYAM-MH-12-0004561",
                LocalDate.of(2018, 5, 20),
                "Private Limited Company",
                "Textiles",
                "Plot No 45, MIDC",
                null,
                "Nagpur",
                "Maharashtra",
                "440012",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Mockito.when(businessService.getBusinessByOwner(any())).thenReturn(response);

        mockMvc.perform(get("/business/my-business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalName").value("Saraswati Fabrics Private Limited"))
                .andExpect(jsonPath("$.gstin").value("22AAAAA1111A1Z1"));
    }

    @Test
    public void testGetMyBusiness_NotFound() throws Exception {
        Mockito.when(businessService.getBusinessByOwner(any()))
                .thenThrow(new ResourceNotFoundException("No business profile found for this user owner."));

        mockMvc.perform(get("/business/my-business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No business profile found for this user owner."));
    }
}
