package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Load Test Simulation
 * Simulates realistic user load with concurrent users
 */
public class LoadTestSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int USERS = Integer.getInteger("users", 50);
    private static final int DURATION_MINUTES = Integer.getInteger("duration", 5);

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Load Test");

    // Municipality data for random selection
    private static final String[] MUNICIPALITIES = {
        "Lisboa", "Porto", "Braga", "Coimbra", "Faro", 
        "Aveiro", "Évora", "Setúbal", "Viseu", "Beja"
    };

    private static final String[] TIME_SLOTS = {
        "MORNING", "AFTERNOON", "EVENING"
    };

    // Feeder for random data
    FeederBuilder<Object> municipalityFeeder = listFeeder(
        java.util.stream.IntStream.range(0, MUNICIPALITIES.length)
            .mapToObj(i -> java.util.Map.<String, Object>of(
                "municipality", MUNICIPALITIES[i],
                "municipalityCode", MUNICIPALITIES[i].substring(0, 4).toUpperCase() + "01"
            ))
            .toList()
    ).random();

    FeederBuilder<Object> timeSlotFeeder = listFeeder(
        java.util.Arrays.stream(TIME_SLOTS)
            .map(slot -> java.util.Map.<String, Object>of("timeSlot", slot))
            .toList()
    ).random();

    FeederBuilder<Object> userDataFeeder = listFeeder(
        java.util.stream.IntStream.range(1, 1000)
            .mapToObj(i -> java.util.Map.<String, Object>of(
                "userId", i,
                "userName", "User " + i,
                "userEmail", "user" + i + "@example.com",
                "userPhone", "91234" + String.format("%04d", i)
            ))
            .toList()
    ).circular();

    // Realistic User Journey: Citizen creates request
    ScenarioBuilder citizenJourney = scenario("Citizen Creates Service Request")
        .feed(municipalityFeeder)
        .feed(timeSlotFeeder)
        .feed(userDataFeeder)
        .exec(http("Get Municipalities")
            .get("/api/municipalities")
            .check(status().is(200)))
        .pause(2, 5) // User thinks about which municipality
        .exec(http("Create Service Request")
            .post("/api/requests")
            .body(StringBody(session -> """
                {
                  "municipalityCode": "%s",
                  "municipalityName": "%s",
                  "citizenName": "%s",
                  "citizenEmail": "%s",
                  "citizenPhone": "%s",
                  "pickupAddress": "Rua Test %s, %s",
                  "itemDescription": "Load test - household items for disposal",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "%s"
                }
                """.formatted(
                    session.getString("municipalityCode"),
                    session.getString("municipality"),
                    session.getString("userName"),
                    session.getString("userEmail"),
                    session.getString("userPhone"),
                    session.getInt("userId"),
                    session.getString("municipality"),
                    LocalDate.now().plusDays(new Random().nextInt(14) + 1),
                    session.getString("timeSlot")
                )))
            .check(status().is(201))
            .check(jsonPath("$.token").saveAs("token")))
        .pause(1, 3)
        .exec(http("Check Request Status")
            .get("/api/requests/#{token}")
            .check(status().is(200)))
        .pause(2);

    // Realistic User Journey: Citizen checks existing request
    ScenarioBuilder checkRequestJourney = scenario("Citizen Checks Request Status")
        .feed(userDataFeeder)
        .feed(municipalityFeeder)
        .feed(timeSlotFeeder)
        .exec(http("Create Request First")
            .post("/api/requests")
            .body(StringBody(session -> """
                {
                  "municipalityCode": "%s",
                  "municipalityName": "%s",
                  "citizenName": "%s",
                  "citizenEmail": "%s",
                  "citizenPhone": "%s",
                  "pickupAddress": "Rua Check %s",
                  "itemDescription": "Items for checking status",
                  "preferredDate": "%s",
                  "preferredTimeSlot": "%s"
                }
                """.formatted(
                    session.getString("municipalityCode"),
                    session.getString("municipality"),
                    session.getString("userName"),
                    session.getString("userEmail"),
                    session.getString("userPhone"),
                    session.getInt("userId"),
                    LocalDate.now().plusDays(10),
                    session.getString("timeSlot")
                )))
            .check(status().is(201))
            .check(jsonPath("$.token").saveAs("token")))
        .pause(5, 10) // User comes back later
        .exec(http("Check Status")
            .get("/api/requests/#{token}")
            .check(status().is(200)))
        .pause(2);

    // Staff viewing requests
    ScenarioBuilder staffViewRequests = scenario("Staff Views Requests")
        .exec(http("Get All Requests")
            .get("/api/staff/requests")
            .check(status().is(200)))
        .pause(3, 5)
        .exec(http("Filter by Municipality")
            .get("/api/staff/requests?municipality=Lisboa")
            .check(status().is(200)))
        .pause(2, 4)
        .exec(http("Filter by Status")
            .get("/api/staff/requests?status=RECEIVED")
            .check(status().is(200)))
        .pause(2);

    // Load Profile
    {
        setUp(
            citizenJourney.injectOpen(
                rampUsers(USERS).during(Duration.ofSeconds(30)),
                constantUsersPerSec(USERS / 10.0).during(Duration.ofMinutes(DURATION_MINUTES))
            ),
            checkRequestJourney.injectOpen(
                rampUsers(USERS / 2).during(Duration.ofSeconds(20)),
                constantUsersPerSec(USERS / 20.0).during(Duration.ofMinutes(DURATION_MINUTES))
            ),
            staffViewRequests.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(10)),
                constantUsersPerSec(1).during(Duration.ofMinutes(DURATION_MINUTES))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(10000),
             global().responseTime().percentile3().lt(5000),
             global().successfulRequests().percent().gt(90.0),
             forAll().failedRequests().count().lt(100L)
         );
    }
}

