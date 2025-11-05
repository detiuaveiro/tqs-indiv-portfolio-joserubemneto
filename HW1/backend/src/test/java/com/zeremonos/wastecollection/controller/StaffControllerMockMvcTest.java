package com.zeremonos.wastecollection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("Staff Controller - MockMvc Integration Tests")
class StaffControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @BeforeEach
    void setUp() {
        serviceRequestRepository.deleteAll();
    }

    @Test
    @DisplayName("Should get all requests without filter")
    void testGetAllRequests_NoFilter_Returns200() throws Exception {
        // Create test data
        createRequest("Lisboa", RequestStatus.RECEIVED);
        createRequest("Porto", RequestStatus.ASSIGNED);
        createRequest("Braga", RequestStatus.COMPLETED);

        mockMvc.perform(get("/api/staff/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].municipalityName", containsInAnyOrder("Lisboa", "Porto", "Braga")));
    }

    @Test
    @DisplayName("Should filter requests by municipality")
    void testGetAllRequests_WithMunicipalityFilter_Returns200() throws Exception {
        // Create test data
        createRequest("Lisboa", RequestStatus.RECEIVED);
        createRequest("Lisboa", RequestStatus.ASSIGNED);
        createRequest("Porto", RequestStatus.RECEIVED);

        mockMvc.perform(get("/api/staff/requests")
                .param("municipality", "Lisboa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].municipalityName", everyItem(is("Lisboa"))));
    }

    @Test
    @DisplayName("Should return empty array when no requests match filter")
    void testGetAllRequests_NoMatches_ReturnsEmptyArray() throws Exception {
        createRequest("Lisboa", RequestStatus.RECEIVED);

        mockMvc.perform(get("/api/staff/requests")
                .param("municipality", "Faro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return empty array when no requests exist")
    void testGetAllRequests_NoData_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/staff/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should update status from RECEIVED to ASSIGNED")
    void testUpdateStatus_ReceivedToAssigned_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.ASSIGNED);
        updateRequest.setNotes("Assigned to Team A");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("Should update status from ASSIGNED to IN_PROGRESS")
    void testUpdateStatus_AssignedToInProgress_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.ASSIGNED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.IN_PROGRESS);
        updateRequest.setNotes("Collection started");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should update status from IN_PROGRESS to COMPLETED")
    void testUpdateStatus_InProgressToCompleted_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.IN_PROGRESS);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.COMPLETED);
        updateRequest.setNotes("Collection completed successfully");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should reject invalid status transition RECEIVED to COMPLETED")
    void testUpdateStatus_InvalidTransition_Returns400() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.COMPLETED);

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid status transition")));
    }

    @Test
    @DisplayName("Should reject update of completed request")
    void testUpdateStatus_CompletedRequest_Returns400() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.COMPLETED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.ASSIGNED);

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot change status of completed request")));
    }

    @Test
    @DisplayName("Should allow cancelling request from any active status")
    void testUpdateStatus_CancelFromReceived_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.CANCELLED);
        updateRequest.setNotes("Cancelled by staff");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should allow reopening cancelled request")
    void testUpdateStatus_ReopenCancelled_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.CANCELLED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.RECEIVED);
        updateRequest.setNotes("Reopened by staff");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    @DisplayName("Should reject cancelled to assigned transition")
    void testUpdateStatus_CancelledToAssigned_Returns400() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.CANCELLED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.ASSIGNED);

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Can only reopen cancelled requests to RECEIVED")));
    }

    @Test
    @DisplayName("Should return 400 when new status is missing")
    void testUpdateStatus_MissingStatus_Returns400() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        // newStatus is null

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newStatus").exists());
    }

    @Test
    @DisplayName("Should return 404 when request not found")
    void testUpdateStatus_RequestNotFound_Returns404() throws Exception {
        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.ASSIGNED);

        mockMvc.perform(put("/api/staff/requests/{id}/status", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @DisplayName("Should update status without notes")
    void testUpdateStatus_WithoutNotes_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.ASSIGNED);
        // notes is null

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("Should handle multiple status updates sequentially")
    void testUpdateStatus_MultipleUpdates_Success() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        // Update 1: RECEIVED -> ASSIGNED
        UpdateStatusRequest update1 = new UpdateStatusRequest();
        update1.setNewStatus(RequestStatus.ASSIGNED);
        update1.setNotes("Assigned");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk());

        // Update 2: ASSIGNED -> IN_PROGRESS
        UpdateStatusRequest update2 = new UpdateStatusRequest();
        update2.setNewStatus(RequestStatus.IN_PROGRESS);
        update2.setNotes("Started");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isOk());

        // Update 3: IN_PROGRESS -> COMPLETED
        UpdateStatusRequest update3 = new UpdateStatusRequest();
        update3.setNewStatus(RequestStatus.COMPLETED);
        update3.setNotes("Completed");

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should handle concurrent requests for different municipalities")
    void testGetAllRequests_MultipleMunicipalities_ReturnsAll() throws Exception {
        createRequest("Lisboa", RequestStatus.RECEIVED);
        createRequest("Porto", RequestStatus.ASSIGNED);
        createRequest("Faro", RequestStatus.IN_PROGRESS);
        createRequest("Braga", RequestStatus.COMPLETED);
        createRequest("Coimbra", RequestStatus.CANCELLED);

        mockMvc.perform(get("/api/staff/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    @DisplayName("Should handle long notes in status update")
    void testUpdateStatus_LongNotes_Returns200() throws Exception {
        ServiceRequest request = createRequest("Lisboa", RequestStatus.RECEIVED);

        UpdateStatusRequest updateRequest = new UpdateStatusRequest();
        updateRequest.setNewStatus(RequestStatus.ASSIGNED);
        updateRequest.setNotes("A".repeat(500)); // Max length

        mockMvc.perform(put("/api/staff/requests/{id}/status", request.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    private ServiceRequest createRequest(String municipality, RequestStatus status) {
        ServiceRequest request = new ServiceRequest();
        request.setMunicipalityCode("CODE");
        request.setMunicipalityName(municipality);
        request.setCitizenName("Test Citizen");
        request.setCitizenEmail("test@example.com");
        request.setCitizenPhone("912345678");
        request.setPickupAddress("Test Address, 123");
        request.setItemDescription("Test items for collection and disposal");
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setPreferredTimeSlot(TimeSlot.MORNING);
        request.setStatus(status);
        return serviceRequestRepository.save(request);
    }
}

