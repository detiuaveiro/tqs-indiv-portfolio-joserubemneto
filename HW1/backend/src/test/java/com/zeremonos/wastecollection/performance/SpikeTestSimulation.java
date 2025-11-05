package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Spike Test Simulation
 * Tests sudden traffic spikes
 */
public class SpikeTestSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Spike Test");

    ScenarioBuilder userScenario = scenario("Spike Test Scenario")
        .exec(http("Create Request")
            .post("/api/requests")
            .body(StringBody("""
                {
                  "municipalityCode": "SPIKE01",
                  "municipalityName": "Lisboa",
                  "citizenName": "Spike Test User",
                  "citizenEmail": "spike@example.com",
                  "citizenPhone": "912345678",
                  "pickupAddress": "Spike Address",
                  "itemDescription": "Spike test item",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "AFTERNOON"
                }
                """.formatted(LocalDate.now().plusDays(5))))
            .check(status().in(200, 201, 400, 429, 503)));

    // Spike Test: sudden traffic increase
    {
        setUp(
            userScenario.injectOpen(
                nothingFor(Duration.ofSeconds(5)),
                atOnceUsers(100), // Sudden spike
                nothingFor(Duration.ofSeconds(10)),
                atOnceUsers(150), // Even bigger spike
                nothingFor(Duration.ofSeconds(10)),
                rampUsers(50).during(Duration.ofSeconds(5)) // Recovery
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(15000),
             global().successfulRequests().percent().gt(80.0)
         );
    }
}

