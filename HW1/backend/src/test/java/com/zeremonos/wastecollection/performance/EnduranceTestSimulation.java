package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Endurance Test Simulation (Soak Test)
 * Tests system stability over extended period
 */
public class EnduranceTestSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int USERS = Integer.getInteger("users", 20);
    private static final int DURATION_MINUTES = Integer.getInteger("duration", 30);

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Endurance Test");

    // Feeder for varying data
    FeederBuilder<Object> dataFeeder = listFeeder(
        java.util.stream.IntStream.range(1, 10000)
            .mapToObj(i -> java.util.Map.<String, Object>of(
                "id", i,
                "name", "Endurance User " + i,
                "email", "endurance" + i + "@example.com"
            ))
            .toList()
    ).circular();

    ScenarioBuilder enduranceScenario = scenario("Long Running User Activity")
        .feed(dataFeeder)
        .exec(http("Create Request")
            .post("/api/requests")
            .body(StringBody(session -> """
                {
                  "municipalityCode": "END01",
                  "municipalityName": "Lisboa",
                  "citizenName": "%s",
                  "citizenEmail": "%s",
                  "citizenPhone": "912345678",
                  "pickupAddress": "Endurance Address %s",
                  "itemDescription": "Endurance test - continuous operation",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "MORNING"
                }
                """.formatted(
                    session.getString("name"),
                    session.getString("email"),
                    session.getInt("id"),
                    LocalDate.now().plusDays(7)
                )))
            .check(status().is(201))
            .check(jsonPath("$.token").saveAs("token")))
        .pause(5, 10)
        .exec(http("Check Status")
            .get("/api/requests/#{token}")
            .check(status().is(200)))
        .pause(10, 20)
        .repeat(2).on(
            exec(http("Get Municipalities")
                .get("/api/municipalities")
                .check(status().is(200)))
            .pause(5, 10)
        );

    // Endurance Test: Constant load over time
    {
        setUp(
            enduranceScenario.injectOpen(
                rampUsers(USERS).during(Duration.ofMinutes(2)),
                constantUsersPerSec(USERS / 10.0).during(Duration.ofMinutes(DURATION_MINUTES))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().mean().lt(3000),
             global().responseTime().max().lt(10000),
             global().successfulRequests().percent().gt(95.0),
             global().responseTime().percentile3().lt(5000)
         );
    }
}

