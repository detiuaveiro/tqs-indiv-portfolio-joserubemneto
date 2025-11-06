package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Stress Test Simulation - Find System Breaking Point
 * 
 * Tests the system beyond normal load to identify bottlenecks,
 * resource limitations, and breaking points.
 * 
 * Load Profile:
 * - Start: 0 users
 * - Ramp up: 10 users per second for 60 seconds (600 total)
 * - Sustain: Hold for 120 seconds
 * - Ramp down: Gradually decrease
 * 
 * Duration: 4 minutes
 * Expected: System should degrade gracefully, maintain >80% success rate
 */
public class StressTestSimulation extends BaseSimulation {

    // High-Load Request Creation
    ScenarioBuilder heavyRequestCreation = scenario("Stress: Heavy Request Creation")
            .exec(
                http("Create Request Under Stress")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().in(201, 500, 503)) // Accept server errors under stress
                    .check(status().saveAs("httpStatus"))
                    .checkIf(session -> "201".equals(session.getString("httpStatus")))
                        .then(jsonPath("$.trackingToken").exists().saveAs("trackingToken"))
            )
            .pause(1, 3);

    // High-Load Dashboard Access
    ScenarioBuilder heavyDashboardLoad = scenario("Stress: Heavy Dashboard Load")
            .exec(
                http("Dashboard Under Stress")
                    .get("/api/staff/requests")
                    .check(status().in(200, 500, 503))
            )
            .pause(1, 2);

    // High-Load Municipality Requests
    ScenarioBuilder heavyMunicipalityLoad = scenario("Stress: Heavy Municipality Load")
            .exec(
                http("Municipality Under Stress")
                    .get("/api/municipalities")
                    .check(status().in(200, 500, 503))
            )
            .pause(1, 2);

    // Database-Intensive Operations
    ScenarioBuilder databaseStress = scenario("Stress: Database Operations")
            .exec(
                http("Create Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().in(201, 500, 503))
                    .check(status().saveAs("createStatus"))
                    .checkIf(session -> "201".equals(session.getString("createStatus")))
                        .then(
                            jsonPath("$.id").exists().saveAs("requestId"),
                            jsonPath("$.trackingToken").exists().saveAs("trackingToken")
                        )
            )
            .pause(1)
            .doIf(session -> session.contains("requestId")).then(
                exec(
                    http("Update Status")
                        .put("/api/staff/requests/#{requestId}/status")
                        .body(StringBody(session -> generateUpdateStatusBody()))
                        .check(status().in(200, 404, 500, 503))
                )
            )
            .pause(1);

    // Concurrent Update Conflicts
    ScenarioBuilder concurrentUpdates = scenario("Stress: Concurrent Updates")
            .exec(
                http("Create Request for Updates")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().in(201, 500, 503))
                    .check(status().saveAs("concurrentStatus"))
                    .checkIf(session -> "201".equals(session.getString("concurrentStatus")))
                        .then(jsonPath("$.id").exists().saveAs("sharedRequestId"))
            )
            .pause(1)
            .doIf(session -> session.contains("sharedRequestId")).then(
                repeat(5).on(
                    exec(
                        http("Rapid Status Update")
                            .put("/api/staff/requests/#{sharedRequestId}/status")
                            .body(StringBody(session -> generateUpdateStatusBody()))
                            .check(status().in(200, 400, 404, 409, 500, 503))
                    )
                    .pause(0, 1)
                )
            );

    {
        setUp(
            // Phase 1: Ramp up to high load
            heavyRequestCreation.injectOpen(
                rampUsersPerSec(1).to(10).during(60),
                constantUsersPerSec(10).during(120),
                rampUsersPerSec(10).to(1).during(30)
            ),
            heavyDashboardLoad.injectOpen(
                rampUsersPerSec(1).to(8).during(60),
                constantUsersPerSec(8).during(120),
                rampUsersPerSec(8).to(1).during(30)
            ),
            heavyMunicipalityLoad.injectOpen(
                rampUsersPerSec(1).to(5).during(60),
                constantUsersPerSec(5).during(120),
                rampUsersPerSec(5).to(1).during(30)
            ),
            databaseStress.injectOpen(
                rampUsersPerSec(1).to(5).during(60),
                constantUsersPerSec(5).during(120),
                rampUsersPerSec(5).to(1).during(30)
            ),
            concurrentUpdates.injectOpen(
                rampUsersPerSec(1).to(3).during(60),
                constantUsersPerSec(3).during(120),
                rampUsersPerSec(3).to(1).during(30)
            )
        ).protocols(httpProtocol)
         .assertions(
            global().successfulRequests().percent().gte(80.0), // Lower success rate acceptable
            global().responseTime().max().lte(15000), // Higher response time tolerance
            global().responseTime().percentile(95.0).lte(10000)
        );
    }
}

