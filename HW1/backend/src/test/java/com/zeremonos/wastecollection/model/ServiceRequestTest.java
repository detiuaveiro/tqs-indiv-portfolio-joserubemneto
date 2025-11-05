package com.zeremonos.wastecollection.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRequestTest {

    private ServiceRequest serviceRequest;

    @BeforeEach
    void setUp() {
        serviceRequest = new ServiceRequest();
        serviceRequest.setMunicipalityCode("1301");
        serviceRequest.setMunicipalityName("Lisboa");
        serviceRequest.setCitizenName("João Silva");
        serviceRequest.setCitizenEmail("joao.silva@example.com");
        serviceRequest.setCitizenPhone("912345678");
        serviceRequest.setPickupAddress("Rua Example, 123, Lisboa");
        serviceRequest.setItemDescription("Old mattress and broken refrigerator");
        serviceRequest.setPreferredDate(LocalDate.now().plusDays(5));
        serviceRequest.setPreferredTimeSlot(TimeSlot.MORNING);
    }

    @Test
    void testTokenGeneration() {
        // Simulate @PrePersist behavior
        if (serviceRequest.getToken() == null) {
            serviceRequest.onCreate();
        }
        
        assertNotNull(serviceRequest.getToken(), "Token should be generated");
        assertEquals(36, serviceRequest.getToken().length(), "Token should be a UUID (36 characters)");
    }

    @Test
    void testInitialStatus() {
        assertEquals(RequestStatus.RECEIVED, serviceRequest.getStatus(), 
            "Initial status should be RECEIVED");
    }

    @Test
    void testUpdateStatus() {
        serviceRequest.onCreate(); // Initialize status history
        
        serviceRequest.updateStatus(RequestStatus.ASSIGNED, "Assigned to team A");
        
        assertEquals(RequestStatus.ASSIGNED, serviceRequest.getStatus(), 
            "Status should be updated to ASSIGNED");
        assertTrue(serviceRequest.getStatusHistory().size() >= 2, 
            "Status history should contain at least 2 entries");
    }

    @Test
    void testAddStatusChange() {
        serviceRequest.addStatusChange(null, RequestStatus.RECEIVED, "Initial status");
        
        assertEquals(1, serviceRequest.getStatusHistory().size(), 
            "Status history should contain 1 entry");
        
        StatusHistory history = serviceRequest.getStatusHistory().get(0);
        assertNull(history.getPreviousStatus(), "Previous status should be null for first entry");
        assertEquals(RequestStatus.RECEIVED, history.getNewStatus(), 
            "New status should be RECEIVED");
        assertEquals("Initial status", history.getNotes(), 
            "Notes should match");
    }

    @Test
    void testMunicipalityFields() {
        assertEquals("1301", serviceRequest.getMunicipalityCode());
        assertEquals("Lisboa", serviceRequest.getMunicipalityName());
    }

    @Test
    void testCitizenInformation() {
        assertEquals("João Silva", serviceRequest.getCitizenName());
        assertEquals("joao.silva@example.com", serviceRequest.getCitizenEmail());
        assertEquals("912345678", serviceRequest.getCitizenPhone());
    }

    @Test
    void testPickupDetails() {
        assertEquals("Rua Example, 123, Lisboa", serviceRequest.getPickupAddress());
        assertEquals("Old mattress and broken refrigerator", serviceRequest.getItemDescription());
    }

    @Test
    void testPreferredSchedule() {
        assertEquals(LocalDate.now().plusDays(5), serviceRequest.getPreferredDate());
        assertEquals(TimeSlot.MORNING, serviceRequest.getPreferredTimeSlot());
    }
}

