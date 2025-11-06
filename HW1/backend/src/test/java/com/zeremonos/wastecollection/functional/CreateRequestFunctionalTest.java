package com.zeremonos.wastecollection.functional;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreateRequestFunctionalTest extends BaseFunctionalTest {

    @Test
    @Order(1)
    void shouldLoadCreateRequestPage() {
        driver.get(frontendUrl + "/create");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("municipality")));
        
        wait.until(d -> {
            Select select = new Select(d.findElement(By.id("municipality")));
            return select.getOptions().size() > 1;
        });
        
        assertThat(driver.getTitle()).isNotEmpty();
        assertThat(driver.findElement(By.id("municipality")).isDisplayed()).isTrue();
    }

    @Test
    @Order(2)
    void shouldDisplayCharacterCounter() {
        driver.get(frontendUrl + "/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("itemDescription")));

        WebElement itemDescription = driver.findElement(By.id("itemDescription"));
        itemDescription.sendKeys("This is a test description");

        WebElement charCounter = driver.findElement(By.cssSelector(".char-counter"));
        assertThat(charCounter.getText()).contains("26/500");
    }

    @Test
    @Order(3)
    void shouldLoadMunicipalityOptions() {
        driver.get(frontendUrl + "/create");

        WebElement municipality = driver.findElement(By.id("municipality"));
        wait.until(d -> {
            Select select = new Select(d.findElement(By.id("municipality")));
            return select.getOptions().size() > 1;
        });

        Select municipalitySelect = new Select(municipality);
        assertThat(municipalitySelect.getOptions().size()).isGreaterThan(1);
        
        boolean hasLisboa = municipalitySelect.getOptions().stream()
            .anyMatch(option -> option.getText().contains("Lisboa"));
        assertThat(hasLisboa).isTrue();
    }

    @Test
    @Order(4)
    void shouldCreateRequestAndDisplayToken() {
        driver.get(frontendUrl + "/create");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("municipality")));

        wait.until(d -> {
            Select select = new Select(d.findElement(By.id("municipality")));
            return select.getOptions().size() > 1;
        });

        Select municipalitySelect = new Select(driver.findElement(By.id("municipality")));
        municipalitySelect.selectByVisibleText("Lisboa");

        driver.findElement(By.id("pickupAddress")).sendKeys("Rua da Liberdade, 123, 1250-140 Lisboa");
        driver.findElement(By.id("citizenName")).sendKeys("João Silva E2E Test");
        driver.findElement(By.id("citizenEmail")).sendKeys("joao.e2e@example.com");
        driver.findElement(By.id("citizenPhone")).sendKeys("912345678");
        driver.findElement(By.id("itemDescription"))
            .sendKeys("E2E Test - Old refrigerator and washing machine that need collection");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String futureDate = LocalDate.now().plusDays(5).format(formatter);
        WebElement dateField = driver.findElement(By.id("preferredDate"));
        dateField.clear();
        dateField.sendKeys(futureDate);

        Select timeSlotSelect = new Select(driver.findElement(By.id("preferredTimeSlot")));
        timeSlotSelect.selectByValue("MORNING");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try {
            WebElement successAlert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success"))
            );
            
            assertThat(successAlert.isDisplayed()).isTrue();
            assertThat(successAlert.getText()).containsIgnoringCase("success");
            
            WebElement tokenDisplay = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".token-display code"))
            );
            
            String token = tokenDisplay.getText();
            
            assertThat(token)
                .as("Token should be displayed")
                .isNotEmpty();
            
            assertThat(token.length())
                .as("Token should have reasonable length (UUID format)")
                .isGreaterThan(20);
            
            assertThat(token)
                .as("Token should be in UUID format")
                .contains("-");
                
        } catch (TimeoutException e) {
            System.out.println("=== DEBUG INFO ===");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page Title: " + driver.getTitle());
            
            System.out.println("Success alerts: " + driver.findElements(By.cssSelector(".alert-success")).size());
            System.out.println("Error alerts: " + driver.findElements(By.cssSelector(".alert-error")).size());
            System.out.println("Field errors: " + driver.findElements(By.cssSelector(".field-error")).size());
            
            System.out.println("Token elements: " + driver.findElements(By.cssSelector(".token-display code")).size());
            
            if (!driver.findElements(By.cssSelector(".alert-error")).isEmpty()) {
                WebElement errorAlert = driver.findElement(By.cssSelector(".alert-error"));
                System.out.println("Error message: " + errorAlert.getText());
            }
            
            throw new AssertionError("Failed to create request or display token", e);
        }
    }
}
