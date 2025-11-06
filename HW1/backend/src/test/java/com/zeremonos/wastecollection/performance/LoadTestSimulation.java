package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Load Test Simulation - Realistic User Load
 * 
 * Tests the system under realistic expected load conditions.
 * Simulates normal business operations with multiple user types
 * performing various actions simultaneously.
 * 
 * Load Profile:
 * - Citizens creating requests: 30 users over 60 seconds
 * - Citizens tracking requests: 20 users over 60 seconds
 * - Staff viewing dashboard: 15 users over 60 seconds
 * - Staff updating statuses: 10 users over 60 seconds
 * 
 * Duration: 2 minutes
 * Expected: System should handle load with <2s response time and >95% success rate
 */
public class LoadTestSimulation extends BaseSimulation {

    // Citizen Scenario: Create and Track Request
    ScenarioBuilder citizenCreateAndTrack = scenario("Citizen: Create and Track Request")
            .exec(
                http("Create Service Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(jsonPath("$.trackingToken").exists().saveAs("trackingToken"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(5, 10)
            .exec(
                http("Track Request")
                    .get("/api/requests/#{trackingToken}")
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(10, 20);

    // Citizen Scenario: Browse Municipalities
    ScenarioBuilder citizenBrowseMunicipalities = scenario("Citizen: Browse Municipalities")
            .exec(
                http("Get Municipalities")
                    .get("/api/municipalities")
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(3000))
            )
            .pause(2, 5)
            .exec(
                http("Create Request After Browsing")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(3, 7);

    // Staff Scenario: Dashboard Monitoring
    ScenarioBuilder staffMonitorDashboard = scenario("Staff: Monitor Dashboard")
            .repeat(3).on(
                exec(
                    http("View All Requests")
                        .get("/api/staff/requests")
                        .check(status().is(200))
                        .check(responseTimeInMillis().lte(2000))
                )
                .pause(10, 20)
                .exec(
                    http("Filter by Municipality")
                        .get("/api/staff/requests")
                        .queryParam("municipality", session -> randomMunicipalityName())
                        .check(status().is(200))
                        .check(responseTimeInMillis().lte(2000))
                )
                .pause(8, 15)
            );

    // Staff Scenario: Process Requests
    ScenarioBuilder staffProcessRequests = scenario("Staff: Process Requests")
            .exec(
                http("Create Request for Processing")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(jsonPath("$.id").exists().saveAs("requestId"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(2, 4)
            .exec(
                http("Assign Request")
                    .put("/api/staff/requests/#{requestId}/status")
                    .body(StringBody("""
                        {
                            "newStatus": "ASSIGNED",
                            "notes": "Load test - Assigned to team"
                        }
                        """))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1500))
            )
            .pause(5, 10)
            .exec(
                http("Start Progress")
                    .put("/api/staff/requests/#{requestId}/status")
                    .body(StringBody("""
                        {
                            "newStatus": "IN_PROGRESS",
                            "notes": "Load test - Collection started"
                        }
                        """))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1500))
            )
            .pause(5, 10)
            .exec(
                http("Complete Request")
                    .put("/api/staff/requests/#{requestId}/status")
                    .body(StringBody("""
                        {
                            "newStatus": "COMPLETED",
                            "notes": "Load test - Collection completed"
                        }
                        """))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1500))
            )
            .pause(3, 7);

    // Mixed Scenario: Random User Actions
    ScenarioBuilder mixedUserActions = scenario("Mixed: Random User Actions")
            .randomSwitch().on(
                percent(40.0).then(
                    exec(
                        http("Create Request")
                            .post("/api/requests")
                            .body(StringBody(session -> generateServiceRequestBody()))
                            .check(status().is(201))
                    )
                ),
                percent(25.0).then(
                    exec(
                        http("View Dashboard")
                            .get("/api/staff/requests")
                            .check(status().is(200))
                    )
                ),
                percent(20.0).then(
                    exec(
                        http("Get Municipalities")
                            .get("/api/municipalities")
                            .check(status().is(200))
                    )
                ),
                percent(15.0).then(
                    exec(
                        http("Filter Requests")
                            .get("/api/staff/requests")
                            .queryParam("municipality", session -> randomMunicipalityName())
                            .check(status().is(200))
                    )
                )
            )
            .pause(3, 8);

    {
        setUp(
            citizenCreateAndTrack.injectOpen(
                rampUsers(30).during(60)
            ),
            citizenBrowseMunicipalities.injectOpen(
                rampUsers(20).during(60)
            ),
            staffMonitorDashboard.injectOpen(
                rampUsers(15).during(60)
            ),
            staffProcessRequests.injectOpen(
                rampUsers(10).during(60)
            ),
            mixedUserActions.injectOpen(
                constantUsersPerSec(3).during(120)
            )
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lte(5000),
            global().responseTime().mean().lte(2000),
            global().responseTime().percentile(95.0).lte(3000),
            global().successfulRequests().percent().gte(95.0),
            details("Create Service Request").responseTime().mean().lte(2000),
            details("View All Requests").responseTime().mean().lte(2000)
        );
    }
}

