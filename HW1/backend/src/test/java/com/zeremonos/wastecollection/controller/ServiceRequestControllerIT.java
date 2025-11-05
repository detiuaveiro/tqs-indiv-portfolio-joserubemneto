package com.zeremonos.wastecollection.controller;

import com.zeremonos.wastecollection.dto.ServiceRequestDTO;
import com.zeremonos.wastecollection.dto.UpdateStatusRequest;
import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.TimeSlot;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.max-requests-per-municipality-per-day=10"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServiceRequestControllerIT {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void testCreateServiceRequest_Success() {
        ServiceRequestDTO dto = createValidDTO();

        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(201)
            .body("token", notNullValue())
            .body("municipalityName", equalTo("Lisboa"))
            .body("status", equalTo("RECEIVED"))
            .body("citizenName", equalTo("João Silva"));
    }

    @Test
    void testCreateServiceRequest_InvalidData_Returns400() {
        ServiceRequestDTO dto = new ServiceRequestDTO();
        // Missing required fields

        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("errors", notNullValue());
    }

    @Test
    void testCreateServiceRequest_PastDate_Returns400() {
        ServiceRequestDTO dto = createValidDTO();
        dto.setPreferredDate(LocalDate.now().minusDays(1));

        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(400)
            .body("errors.preferredDate", containsString("future"));
    }

    @Test
    void testGetRequestByToken_Success() {
        // First create a request
        ServiceRequestDTO dto = createValidDTO();
        String token = given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(201)
            .extract()
            .path("token");

        // Then retrieve it
        given()
        .when()
            .get("/api/requests/{token}", token)
        .then()
            .statusCode(200)
            .body("token", equalTo(token))
            .body("status", equalTo("RECEIVED"))
            .body("statusHistory", notNullValue())
            .body("statusHistory[0].newStatus", equalTo("RECEIVED"));
    }

    @Test
    void testGetRequestByToken_NotFound_Returns404() {
        given()
        .when()
            .get("/api/requests/{token}", "invalid-token")
        .then()
            .statusCode(404)
            .body("message", containsString("not found"));
    }

    @Test
    void testCancelRequest_Success() {
        // First create a request
        ServiceRequestDTO dto = createValidDTO();
        String token = given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(201)
            .extract()
            .path("token");

        // Then cancel it
        given()
        .when()
            .delete("/api/requests/{token}", token)
        .then()
            .statusCode(204);

        // Verify it's cancelled
        given()
        .when()
            .get("/api/requests/{token}", token)
        .then()
            .statusCode(200)
            .body("status", equalTo("CANCELLED"));
    }

    @Test
    void testStaffGetAllRequests() {
        // Create some requests
        createRequest("Lisboa");
        createRequest("Porto");

        given()
        .when()
            .get("/api/staff/requests")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testStaffGetRequestsByMunicipality() {
        // Create requests for different municipalities
        createRequest("Lisboa");
        createRequest("Lisboa");
        createRequest("Porto");

        given()
            .queryParam("municipality", "Lisboa")
        .when()
            .get("/api/staff/requests")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2))
            .body("[0].municipalityName", equalTo("Lisboa"));
    }

    @Test
    void testStaffUpdateStatus_Success() {
        // Create a request
        ServiceRequestDTO dto = createValidDTO();
        Integer requestIdInt = given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
        
        Long requestId = requestIdInt.longValue();

        // Update status
        UpdateStatusRequest updateRequest = new UpdateStatusRequest(
            RequestStatus.ASSIGNED, 
            "Assigned to team A"
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/staff/requests/{id}/status", requestId)
        .then()
            .statusCode(200)
            .body("status", equalTo("ASSIGNED"))
            .body("statusHistory.size()", equalTo(2))
            .body("statusHistory[0].newStatus", equalTo("ASSIGNED"))
            .body("statusHistory[0].notes", equalTo("Assigned to team A"));
    }

    @Test
    void testStaffUpdateStatus_InvalidTransition_Returns400() {
        // Create a request
        ServiceRequestDTO dto = createValidDTO();
        Integer requestIdInt = given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
        
        Long requestId = requestIdInt.longValue();

        // Try invalid transition (RECEIVED -> COMPLETED)
        UpdateStatusRequest updateRequest = new UpdateStatusRequest(
            RequestStatus.COMPLETED, 
            null
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/staff/requests/{id}/status", requestId)
        .then()
            .statusCode(400)
            .body("message", containsString("Invalid status transition"));
    }

    private ServiceRequestDTO createValidDTO() {
        ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.setMunicipalityCode("1106");
        dto.setMunicipalityName("Lisboa");
        dto.setCitizenName("João Silva");
        dto.setCitizenEmail("joao@example.com");
        dto.setCitizenPhone("912345678");
        dto.setPickupAddress("Rua Example, 123, Lisboa");
        dto.setItemDescription("Old refrigerator and washing machine that need disposal");
        dto.setPreferredDate(LocalDate.now().plusDays(5));
        dto.setPreferredTimeSlot(TimeSlot.MORNING);
        return dto;
    }

    private void createRequest(String municipality) {
        ServiceRequestDTO dto = createValidDTO();
        dto.setMunicipalityName(municipality);
        
        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/requests")
        .then()
            .statusCode(201);
    }
}

