package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Performance test for Municipality API endpoints
 * Tests: GET /api/municipalities
 * 
 * Scenarios tested:
 * - List all municipalities (external API call)
 * - Concurrent municipality requests
 * - Cache effectiveness testing
 * 
 * Load profile: 30 users over 20 seconds
 */
public class MunicipalityEndpointsSimulation extends BaseSimulation {

    // Scenario 1: Simple Get All Municipalities
    ScenarioBuilder getAllMunicipalities = scenario("Get All Municipalities")
            .exec(
                http("Get Municipalities")
                    .get("/api/municipalities")
                    .check(status().is(200))
                    .check(jsonPath("$").ofList().exists())
                    .check(jsonPath("$[0].code").exists())
                    .check(jsonPath("$[0].name").exists())
                    .check(responseTimeInMillis().lte(5000)) // External API might be slower
            )
            .pause(2, 5);

    // Scenario 2: Repeated Requests (Cache Test)
    ScenarioBuilder repeatedRequests = scenario("Repeated Municipality Requests")
            .repeat(5).on(
                exec(
                    http("Get Municipalities (Cached)")
                        .get("/api/municipalities")
                        .check(status().is(200))
                        .check(jsonPath("$").ofList().exists())
                        .check(responseTimeInMillis().lte(3000))
                )
                .pause(1, 2)
            );

    // Scenario 3: High Concurrent Load
    ScenarioBuilder concurrentLoad = scenario("Concurrent Municipality Requests")
            .exec(
                http("Get Municipalities Concurrent")
                    .get("/api/municipalities")
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(5000))
            )
            .pause(1, 3);

    {
        setUp(
            getAllMunicipalities.injectOpen(
                rampUsers(15).during(10)
            ),
            repeatedRequests.injectOpen(
                rampUsers(10).during(15)
            ),
            concurrentLoad.injectOpen(
                atOnceUsers(20)
            )
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lte(10000), // External API tolerance
            global().successfulRequests().percent().gte(90.0), // Allow some external API failures
            forAll().responseTime().mean().lte(3000)
        );
    }
}

