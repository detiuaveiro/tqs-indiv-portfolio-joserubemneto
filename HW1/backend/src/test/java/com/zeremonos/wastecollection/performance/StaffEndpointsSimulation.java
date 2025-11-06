package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Performance test for Staff API endpoints
 * Tests: GET /api/staff/requests, PUT /api/staff/requests/{id}/status
 * 
 * Scenarios tested:
 * - List all requests
 * - Filter requests by municipality
 * - Update request status
 * 
 * Load profile: 15 users over 20 seconds
 */
public class StaffEndpointsSimulation extends BaseSimulation {

    // Scenario 1: List All Requests
    ScenarioBuilder listAllRequests = scenario("List All Requests")
            .exec(
                http("Get All Requests")
                    .get("/api/staff/requests")
                    .check(status().is(200))
                    .check(jsonPath("$").ofList().exists())
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(2, 5);

    // Scenario 2: Filter Requests by Municipality
    ScenarioBuilder filterByMunicipality = scenario("Filter Requests by Municipality")
            .exec(
                http("Get Requests for Lisboa")
                    .get("/api/staff/requests")
                    .queryParam("municipality", "Lisboa")
                    .check(status().is(200))
                    .check(jsonPath("$").ofList().exists())
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(1, 3)
            .exec(
                http("Get Requests for Porto")
                    .get("/api/staff/requests")
                    .queryParam("municipality", "Porto")
                    .check(status().is(200))
                    .check(jsonPath("$").ofList().exists())
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(2, 4);

    // Scenario 3: Create Request and Update Status (Full Workflow)
    ScenarioBuilder createAndUpdateStatus = scenario("Create Request and Update Status")
            .exec(
                http("Create Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(jsonPath("$.id").exists().saveAs("requestId"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(1, 2)
            .exec(
                http("Update Status to ASSIGNED")
                    .put("/api/staff/requests/#{requestId}/status")
                    .body(StringBody("""
                        {
                            "newStatus": "ASSIGNED",
                            "notes": "Assigned to collection team"
                        }
                        """))
                    .check(status().is(200))
                    .check(jsonPath("$.status").is("ASSIGNED"))
                    .check(responseTimeInMillis().lte(1500))
            )
            .pause(1, 2)
            .exec(
                http("Update Status to IN_PROGRESS")
                    .put("/api/staff/requests/#{requestId}/status")
                    .body(StringBody("""
                        {
                            "newStatus": "IN_PROGRESS",
                            "notes": "Collection in progress"
                        }
                        """))
                    .check(status().is(200))
                    .check(jsonPath("$.status").is("IN_PROGRESS"))
                    .check(responseTimeInMillis().lte(1500))
            )
            .pause(1, 2)
            .exec(
                http("Update Status to COMPLETED")
                    .put("/api/staff/requests/#{requestId}/status")
                    .body(StringBody("""
                        {
                            "newStatus": "COMPLETED",
                            "notes": "Collection completed successfully"
                        }
                        """))
                    .check(status().is(200))
                    .check(jsonPath("$.status").is("COMPLETED"))
                    .check(responseTimeInMillis().lte(1500))
            )
            .pause(2, 4);

    // Scenario 4: Concurrent Dashboard Viewing
    ScenarioBuilder concurrentDashboardViewing = scenario("Concurrent Dashboard Viewing")
            .repeat(5).on(
                exec(
                    http("Refresh Dashboard")
                        .get("/api/staff/requests")
                        .check(status().is(200))
                        .check(responseTimeInMillis().lte(2000))
                )
                .pause(3, 7)
            );

    {
        setUp(
            listAllRequests.injectOpen(
                rampUsers(10).during(15)
            ),
            filterByMunicipality.injectOpen(
                rampUsers(8).during(12)
            ),
            createAndUpdateStatus.injectOpen(
                rampUsers(6).during(20)
            ),
            concurrentDashboardViewing.injectOpen(
                constantUsersPerSec(2).during(30)
            )
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lte(5000),
            global().successfulRequests().percent().gte(95.0),
            forAll().responseTime().mean().lte(1500)
        );
    }
}

