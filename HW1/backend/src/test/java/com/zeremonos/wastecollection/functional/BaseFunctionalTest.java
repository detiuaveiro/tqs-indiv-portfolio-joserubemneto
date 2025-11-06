package com.zeremonos.wastecollection.functional;

import com.zeremonos.wastecollection.model.RequestStatus;
import com.zeremonos.wastecollection.model.ServiceRequest;
import com.zeremonos.wastecollection.model.TimeSlot;
import com.zeremonos.wastecollection.repository.ServiceRequestRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("functional-test")
public abstract class BaseFunctionalTest {

    @Autowired
    protected ServiceRequestRepository serviceRequestRepository;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String frontendUrl = "http://localhost:5173";
    protected String backendUrl = "http://localhost:8080";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        System.out.println("🚀 Starting E2E Tests with H2 in-memory database");
    }

    @BeforeEach
    void setupBrowser() {
        ChromeOptions options = new ChromeOptions();
        
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @BeforeEach
    void setupTestData() {
        serviceRequestRepository.deleteAll();
        
        createTestRequest("John Doe", "john@example.com", RequestStatus.RECEIVED);
        createTestRequest("Jane Smith", "jane@example.com", RequestStatus.ASSIGNED);
        createTestRequest("Bob Johnson", "bob@example.com", RequestStatus.IN_PROGRESS);
        createTestRequest("Alice Brown", "alice@example.com", RequestStatus.COMPLETED);
        createTestRequest("Charlie Wilson", "charlie@example.com", RequestStatus.CANCELLED);
        
        System.out.println("✅ Test data created: 5 requests with different statuses");
    }

    protected ServiceRequest createTestRequest(String name, String email, RequestStatus status) {
        ServiceRequest request = new ServiceRequest();
        request.setMunicipalityCode("LISB34");
        request.setMunicipalityName("Lisboa");
        request.setCitizenName(name);
        request.setCitizenEmail(email);
        request.setCitizenPhone("912345678");
        request.setPickupAddress("Rua de Teste, 123, 1000-000 Lisboa");
        request.setItemDescription("E2E Test - " + name + " - Status: " + status);
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setPreferredTimeSlot(TimeSlot.MORNING);
        request.setStatus(status);
        
        return serviceRequestRepository.save(request);
    }

    @AfterEach
    void teardownBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterAll
    static void cleanupClass() {
        System.out.println("🧹 Cleanup completed");
    }
}

