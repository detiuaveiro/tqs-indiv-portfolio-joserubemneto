package com.zeremonos.wastecollection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeremonos.wastecollection.dto.ServiceRequestDTO;
import com.zeremonos.wastecollection.dto.UpdateStatusRequest;
import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.TimeSlot;
import com.zeremonos.wastecollection.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Citizen Controller - MockMvc Integration Tests")
class CitizenControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    private ServiceRequestDTO validDTO;

    @BeforeEach
    void setUp() {
        serviceRequestRepository.deleteAll();

        validDTO = new ServiceRequestDTO();
        validDTO.setMunicipalityCode("LISB01");
        validDTO.setMunicipalityName("Lisboa");
        validDTO.setCitizenName("João Silva");
        validDTO.setCitizenEmail("joao@example.com");
        validDTO.setCitizenPhone("912345678");
        validDTO.setPickupAddress("Rua Example, 123, Lisboa");
        validDTO.setItemDescription("Old refrigerator and washing machine that need disposal");
        validDTO.setPreferredDate(LocalDate.now().plusDays(5));
        validDTO.setPreferredTimeSlot(TimeSlot.MORNING);
    }

    @Test
    @DisplayName("Should create service request with valid data")
    void testCreateRequest_ValidData_Returns201() throws Exception {
        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.municipalityName").value("Lisboa"))
                .andExpect(jsonPath("$.citizenName").value("João Silva"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Should return 400 when municipality code is missing")
    void testCreateRequest_MissingMunicipalityCode_Returns400() throws Exception {
        validDTO.setMunicipalityCode(null);

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.municipalityCode").exists());
    }

    @Test
    @DisplayName("Should return 400 when citizen name is missing")
    void testCreateRequest_MissingCitizenName_Returns400() throws Exception {
        validDTO.setCitizenName(null);

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.citizenName").exists());
    }

    @Test
    @DisplayName("Should return 400 when email format is invalid")
    void testCreateRequest_InvalidEmail_Returns400() throws Exception {
        validDTO.setCitizenEmail("invalid-email");

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.citizenEmail").exists());
    }

    @Test
    @DisplayName("Should return 400 when phone format is invalid")
    void testCreateRequest_InvalidPhone_Returns400() throws Exception {
        validDTO.setCitizenPhone("123"); // Too short

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.citizenPhone").exists());
    }

    @Test
    @DisplayName("Should return 400 when preferred date is in the past")
    void testCreateRequest_PastDate_Returns400() throws Exception {
        validDTO.setPreferredDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.preferredDate").exists());
    }

    @Test
    @DisplayName("Should return 400 when item description is too short")
    void testCreateRequest_ShortDescription_Returns400() throws Exception {
        validDTO.setItemDescription("ABC"); // Less than 5 chars

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.itemDescription").exists());
    }

    @Test
    @DisplayName("Should return 400 when multiple fields are invalid")
    void testCreateRequest_MultipleInvalidFields_Returns400() throws Exception {
        validDTO.setCitizenName(null);
        validDTO.setCitizenEmail("invalid");
        validDTO.setCitizenPhone("123");

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.citizenName").exists())
                .andExpect(jsonPath("$.errors.citizenEmail").exists())
                .andExpect(jsonPath("$.errors.citizenPhone").exists());
    }

    @Test
    @DisplayName("Should get request by token successfully")
    void testGetRequestByToken_ValidToken_Returns200() throws Exception {
        // Create a request first
        String response = mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // Get by token
        mockMvc.perform(get("/api/requests/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.municipalityName").value("Lisboa"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.statusHistory").isArray());
    }

    @Test
    @DisplayName("Should return 404 when token not found")
    void testGetRequestByToken_InvalidToken_Returns404() throws Exception {
        mockMvc.perform(get("/api/requests/{token}", "invalid-token-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @DisplayName("Should cancel request successfully")
    void testCancelRequest_ValidToken_Returns204() throws Exception {
        // Create a request first
        String response = mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // Cancel it
        mockMvc.perform(delete("/api/requests/{token}", token))
                .andExpect(status().isNoContent());

        // Verify it's cancelled
        mockMvc.perform(get("/api/requests/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should return 400 when trying to cancel completed request")
    void testCancelRequest_CompletedRequest_Returns400() throws Exception {
        // Create and complete a request
        ServiceRequest request = createAndSaveRequest();
        request.setStatus(RequestStatus.COMPLETED);
        serviceRequestRepository.save(request);

        mockMvc.perform(delete("/api/requests/{token}", request.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot cancel a completed request")));
    }

    @Test
    @DisplayName("Should return 400 when trying to cancel already cancelled request")
    void testCancelRequest_AlreadyCancelled_Returns400() throws Exception {
        // Create and cancel a request
        ServiceRequest request = createAndSaveRequest();
        request.setStatus(RequestStatus.CANCELLED);
        serviceRequestRepository.save(request);

        mockMvc.perform(delete("/api/requests/{token}", request.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already cancelled")));
    }

    @Test
    @DisplayName("Should return 404 when cancelling non-existent request")
    void testCancelRequest_InvalidToken_Returns404() throws Exception {
        mockMvc.perform(delete("/api/requests/{token}", "non-existent-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle CORS headers")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/requests")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept request without optional email")
    void testCreateRequest_WithoutEmail_Returns201() throws Exception {
        validDTO.setCitizenEmail(null);

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.citizenEmail").doesNotExist());
    }

    @Test
    @DisplayName("Should create multiple requests for same municipality on same date")
    void testCreateMultipleRequests_SameMunicipalitySameDate_Success() throws Exception {
        // Create first request
        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated());

        // Create second request with different citizen
        validDTO.setCitizenName("Maria Santos");
        validDTO.setCitizenPhone("923456789");

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should reject request when daily limit is reached")
    void testCreateRequest_DailyLimitReached_Returns400() throws Exception {
        // Create 10 requests (the limit)
        for (int i = 0; i < 10; i++) {
            validDTO.setCitizenName("Citizen " + i);
            validDTO.setCitizenPhone("91234567" + i);
            mockMvc.perform(post("/api/requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validDTO)))
                    .andExpect(status().isCreated());
        }

        // 11th request should fail
        validDTO.setCitizenName("Citizen 11");
        validDTO.setCitizenPhone("912345670");

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Daily limit reached")));
    }

    private ServiceRequest createAndSaveRequest() {
        ServiceRequest request = new ServiceRequest();
        request.setMunicipalityCode("LISB01");
        request.setMunicipalityName("Lisboa");
        request.setCitizenName("Test User");
        request.setCitizenEmail("test@example.com");
        request.setCitizenPhone("912345678");
        request.setPickupAddress("Test Address");
        request.setItemDescription("Test items for disposal");
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setPreferredTimeSlot(TimeSlot.MORNING);
        request.setStatus(RequestStatus.RECEIVED);
        return serviceRequestRepository.save(request);
    }
}

