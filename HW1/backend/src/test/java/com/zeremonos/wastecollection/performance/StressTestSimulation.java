package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Stress Test Simulation
 * Tests system behavior under extreme load
 */
public class StressTestSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int MAX_USERS = Integer.getInteger("maxUsers", 200);

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Stress Test")
        .shareConnections(); // Reuse connections

    // Simple scenario for stress testing
    ScenarioBuilder stressScenario = scenario("High Load Request Creation")
        .exec(http("Create Request")
            .post("/api/requests")
            .body(StringBody(session -> """
                {
                  "municipalityCode": "STRESS%s",
                  "municipalityName": "StressTest",
                  "citizenName": "Stress User %s",
                  "citizenEmail": "stress%s@example.com",
                  "citizenPhone": "912345678",
                  "pickupAddress": "Stress Address %s",
                  "itemDescription": "Stress test item description",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "MORNING"
                }
                """.formatted(
                    session.get("userId"),
                    session.get("userId"),
                    session.get("userId"),
                    session.get("userId"),
                    LocalDate.now().plusDays(7)
                )))
            .check(status().in(200, 201, 400, 429))); // Accept rate limiting

    ScenarioBuilder readScenario = scenario("High Load Read Requests")
        .exec(http("Get All Municipalities")
            .get("/api/municipalities")
            .check(status().in(200, 429, 503)))
        .exec(http("Get All Requests")
            .get("/api/staff/requests")
            .check(status().in(200, 429, 503)));

    // Stress Test Load Profile
    {
        setUp(
            stressScenario.injectOpen(
                incrementUsersPerSec(10)
                    .times(5)
                    .eachLevelLasting(Duration.ofSeconds(30))
                    .startingFrom(10)
            ),
            readScenario.injectOpen(
                incrementUsersPerSec(5)
                    .times(5)
                    .eachLevelLasting(Duration.ofSeconds(30))
                    .startingFrom(5)
            )
        ).protocols(httpProtocol)
         .maxDuration(Duration.ofMinutes(3))
         .assertions(
             global().responseTime().max().lt(30000), // More lenient under stress
             global().successfulRequests().percent().gt(70.0) // Lower threshold for stress
         );
    }
}

