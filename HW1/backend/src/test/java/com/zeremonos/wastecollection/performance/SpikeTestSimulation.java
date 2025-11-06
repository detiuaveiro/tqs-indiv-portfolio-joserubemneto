package com.zeremonos.wastecollection.performance;

import io.gatling.javaapi.core.ScenarioBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Spike Test Simulation - Sudden Load Increases
 * 
 * Tests how the system handles dramatic increases in load.
 * Simulates scenarios like:
 * - Viral social media post about the service
 * - Newsletter announcement
 * - System returning after downtime
 * 
 * Load Profile:
 * - Normal load: 10 users for 30 seconds
 * - Sudden spike: 100 users instantly
 * - Sustained spike: 30 seconds
 * - Return to normal: 10 users for 30 seconds
 * 
 * Duration: 2 minutes
 * Expected: System should handle spike gracefully and recover
 */
public class SpikeTestSimulation extends BaseSimulation {

    // Scenario: Normal Traffic Pattern
    ScenarioBuilder normalTraffic = scenario("Normal Traffic")
            .exec(
                http("Normal Request Creation")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().in(201, 500, 503))
            )
            .pause(3, 5)
            .exec(
                http("Normal Dashboard View")
                    .get("/api/staff/requests")
                    .check(status().in(200, 500, 503))
            )
            .pause(3, 5);

    // Scenario: Spike Traffic - Request Creation
    ScenarioBuilder spikeRequestCreation = scenario("Spike: Request Creation")
            .exec(
                http("Spike Create Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().in(201, 429, 500, 503)) // Accept rate limiting
                    .check(status().saveAs("spikeStatus"))
                    .checkIf(session -> "201".equals(session.getString("spikeStatus")))
                        .then(jsonPath("$.trackingToken").exists().saveAs("trackingToken"))
            )
            .pause(1, 2);

    // Scenario: Spike Traffic - Dashboard Viewing
    ScenarioBuilder spikeDashboardViewing = scenario("Spike: Dashboard Viewing")
            .exec(
                http("Spike Dashboard View")
                    .get("/api/staff/requests")
                    .check(status().in(200, 429, 500, 503))
            )
            .pause(1, 2);

    // Scenario: Spike Traffic - Municipality Queries
    ScenarioBuilder spikeMunicipalityQueries = scenario("Spike: Municipality Queries")
            .exec(
                http("Spike Municipality Query")
                    .get("/api/municipalities")
                    .check(status().in(200, 429, 500, 503))
            )
            .pause(1, 3);

    // Scenario: Spike Traffic - Mixed Operations
    ScenarioBuilder spikeMixedOperations = scenario("Spike: Mixed Operations")
            .randomSwitch().on(
                percent(50.0).then(
                    exec(
                        http("Spike Create")
                            .post("/api/requests")
                            .body(StringBody(session -> generateServiceRequestBody()))
                            .check(status().in(201, 429, 500, 503))
                    )
                ),
                percent(30.0).then(
                    exec(
                        http("Spike View")
                            .get("/api/staff/requests")
                            .check(status().in(200, 429, 500, 503))
                    )
                ),
                percent(20.0).then(
                    exec(
                        http("Spike Municipality")
                            .get("/api/municipalities")
                            .check(status().in(200, 429, 500, 503))
                    )
                )
            )
            .pause(1, 2);

    // Scenario: Post-Spike Recovery
    ScenarioBuilder postSpikeRecovery = scenario("Post-Spike Recovery")
            .exec(
                http("Recovery Request")
                    .post("/api/requests")
                    .body(StringBody(session -> generateServiceRequestBody()))
                    .check(status().is(201))
                    .check(responseTimeInMillis().lte(3000))
            )
            .pause(3, 6)
            .exec(
                http("Recovery Dashboard")
                    .get("/api/staff/requests")
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(2500))
            )
            .pause(3, 6);

    {
        setUp(
            // Phase 1: Normal Load (30 seconds)
            normalTraffic.injectOpen(
                constantUsersPerSec(2).during(30)
            ),
            
            // Phase 2: SPIKE! (Instant surge at t=30)
            spikeRequestCreation.injectOpen(
                nothingFor(30),
                atOnceUsers(50)
            ),
            spikeDashboardViewing.injectOpen(
                nothingFor(30),
                atOnceUsers(30)
            ),
            spikeMunicipalityQueries.injectOpen(
                nothingFor(30),
                atOnceUsers(20)
            ),
            spikeMixedOperations.injectOpen(
                nothingFor(30),
                atOnceUsers(30),
                constantUsersPerSec(5).during(30)
            ),
            
            // Phase 3: Recovery (t=60 onwards)
            postSpikeRecovery.injectOpen(
                nothingFor(60),
                rampUsers(15).during(30)
            )
        ).protocols(httpProtocol)
         .assertions(
            // During spike, accept higher failure rate
            global().successfulRequests().percent().gte(70.0),
            global().responseTime().max().lte(20000),
            
            // Recovery phase should normalize
            details("Recovery Request").successfulRequests().percent().gte(95.0),
            details("Recovery Request").responseTime().mean().lte(3000),
            details("Recovery Dashboard").successfulRequests().percent().gte(95.0)
        );
    }
}

