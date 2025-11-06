package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Performance test for Citizen API endpoints
 * Tests: POST /api/requests, GET /api/requests/{token}, DELETE /api/requests/{token}
 * 
 * Scenarios tested:
 * - Create service request
 * - Retrieve request by token
 * - Cancel request by token
 * 
 * Load profile: 20 users over 30 seconds
 */
public class CitizenEndpointsSimulation extends BaseSimulation {

    // Scenario 1: Create Service Request
    ScenarioBuilder createRequest = scenario("Create Service Request")
            .exec(
                http("Create Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(jsonPath("$.trackingToken").exists().saveAs("trackingToken"))
                    .check(jsonPath("$.id").exists().saveAs("requestId"))
                    .check(responseTimeInMillis().lte(2000)) // Response should be under 2 seconds
            )
            .pause(1, 3);

    // Scenario 2: Create and Retrieve Request
    ScenarioBuilder createAndRetrieveRequest = scenario("Create and Retrieve Request")
            .exec(
                http("Create Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(jsonPath("$.trackingToken").exists().saveAs("trackingToken"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(1, 2)
            .exec(
                http("Get Request by Token")
                    .get("/api/requests/#{trackingToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.trackingToken").is("#{trackingToken}"))
                    .check(responseTimeInMillis().lte(1000)) // Retrieve should be faster
            )
            .pause(1, 3);

    // Scenario 3: Create and Cancel Request
    ScenarioBuilder createAndCancelRequest = scenario("Create and Cancel Request")
            .exec(
                http("Create Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(jsonPath("$.trackingToken").exists().saveAs("trackingToken"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .pause(1, 2)
            .exec(
                http("Cancel Request")
                    .delete("/api/requests/#{trackingToken}")
                    .check(status().is(204))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1, 3);

    // Scenario 4: Error handling - Invalid Request
    ScenarioBuilder invalidRequest = scenario("Invalid Service Request")
            .exec(
                http("Create Invalid Request")
                    .post("/api/requests")
                    .body(StringBody("""
                        {
                            "municipalityCode": "",
                            "municipalityName": "",
                            "citizenName": "A",
                            "citizenEmail": "invalid-email",
                            "citizenPhone": "123",
                            "pickupAddress": "Test",
                            "itemDescription": "Short",
                            "preferredDate": "2020-01-01",
                            "preferredTimeSlot": "MORNING"
                        }
                        """))
                    .check(status().is(400))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1, 2);

    {
        setUp(
            createRequest.injectOpen(
                rampUsers(10).during(15)
            ),
            createAndRetrieveRequest.injectOpen(
                rampUsers(15).during(20)
            ),
            createAndCancelRequest.injectOpen(
                rampUsers(8).during(15)
            ),
            invalidRequest.injectOpen(
                rampUsers(5).during(10)
            )
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lte(5000),
            global().successfulRequests().percent().gte(95.0),
            forAll().responseTime().mean().lte(1500)
        );
    }
}

