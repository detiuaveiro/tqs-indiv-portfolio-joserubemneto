package com.zeremonos.wastecollection.bdd.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CucumberHooks {

    private static final Logger log = LoggerFactory.getLogger(CucumberHooks.class);

    @Autowired
    private WebDriverContext webDriverContext;

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("Starting scenario: {}", scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("Scenario failed: {}", scenario.getName());
            
            // Take screenshot on failure
            try {
                WebDriver driver = webDriverContext.getDriver();
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Screenshot on failure");
            } catch (Exception e) {
                log.error("Failed to take screenshot", e);
            }
        }
        
        log.info("Finished scenario: {} - Status: {}", scenario.getName(), scenario.getStatus());
    }

    @After
    public void quitDriver() {
        // Don't quit after each scenario to improve performance
        // Driver will be quit after all tests
    }
}

