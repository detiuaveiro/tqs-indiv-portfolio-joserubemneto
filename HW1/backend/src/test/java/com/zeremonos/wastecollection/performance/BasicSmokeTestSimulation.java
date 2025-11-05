package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Basic Smoke Test Simulation
 * Tests basic functionality with minimal load
 */
public class BasicSmokeTestSimulation extends Simulation {

    // Base URL - can be overridden via system property
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Performance Test");

    // Scenarios
    
    ScenarioBuilder getMunicipalities = scenario("Get Municipalities")
        .exec(http("Get All Municipalities")
            .get("/api/municipalities")
            .check(status().is(200))
            .check(jsonPath("$").ofList().notNull()));

    ScenarioBuilder createServiceRequest = scenario("Create Service Request")
        .exec(http("Create Request")
            .post("/api/requests")
            .body(StringBody("""
                {
                  "municipalityCode": "LISB01",
                  "municipalityName": "Lisboa",
                  "citizenName": "Performance Test User",
                  "citizenEmail": "perftest@example.com",
                  "citizenPhone": "912345678",
                  "pickupAddress": "Rua Test, 123, Lisboa",
                  "itemDescription": "Performance test - old refrigerator",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "MORNING"
                }
                """.formatted(LocalDate.now().plusDays(7))))
            .check(status().is(201))
            .check(jsonPath("$.token").saveAs("token")));

    ScenarioBuilder getServiceRequest = scenario("Get Service Request by Token")
        .exec(http("Create Request First")
            .post("/api/requests")
            .body(StringBody("""
                {
                  "municipalityCode": "PORT01",
                  "municipalityName": "Porto",
                  "citizenName": "Test User",
                  "citizenEmail": "test@example.com",
                  "citizenPhone": "923456789",
                  "pickupAddress": "Rua Porto, 456",
                  "itemDescription": "Test item for retrieval",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "AFTERNOON"
                }
                """.formatted(LocalDate.now().plusDays(5))))
            .check(status().is(201))
            .check(jsonPath("$.token").saveAs("token")))
        .pause(1)
        .exec(http("Get Request by Token")
            .get("/api/requests/#{token}")
            .check(status().is(200))
            .check(jsonPath("$.token").is("#{token}")));

    ScenarioBuilder staffGetAllRequests = scenario("Staff Get All Requests")
        .exec(http("Get All Requests")
            .get("/api/staff/requests")
            .check(status().is(200))
            .check(jsonPath("$").ofList().notNull()));

    // Load Profile: Smoke Test
    {
        setUp(
            getMunicipalities.injectOpen(atOnceUsers(5)),
            createServiceRequest.injectOpen(rampUsers(10).during(Duration.ofSeconds(10))),
            getServiceRequest.injectOpen(rampUsers(5).during(Duration.ofSeconds(5))),
            staffGetAllRequests.injectOpen(atOnceUsers(3))
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(5000),
             global().successfulRequests().percent().gt(95.0)
         );
    }
}

