package com.zeremonos.wastecollection.bdd.steps;

import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonSteps {

    private static final Logger log = LoggerFactory.getLogger(CommonSteps.class);

    @LocalServerPort
    private int port;

    @Given("the application is running")
    public void theApplicationIsRunning() {
        // Check if backend is running
        boolean backendRunning = checkEndpoint("http://localhost:8080/api/municipalities");
        assertTrue(backendRunning, "Backend should be running on port 8080");
        
        // Check if frontend is running
        boolean frontendRunning = checkEndpoint("http://localhost:5173");
        assertTrue(frontendRunning, "Frontend should be running on port 5173");
        
        log.info("Application is running - Backend: {}, Frontend: {}", backendRunning, frontendRunning);
    }

    private boolean checkEndpoint(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 500; // Accept any non-server error
        } catch (Exception e) {
            log.warn("Failed to connect to {}: {}", urlString, e.getMessage());
            return false;
        }
    }
}

