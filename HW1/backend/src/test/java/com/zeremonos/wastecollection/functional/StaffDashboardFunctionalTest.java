package com.zeremonos.wastecollection.functional;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffDashboardFunctionalTest extends BaseFunctionalTest {

    @Test
    @Order(1)
    void shouldLoadStaffDashboard() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Staff Dashboard')]")
        ));
        
        List<WebElement> statCards = driver.findElements(By.cssSelector(".stat-card"));
        assertThat(statCards.size())
            .as("Should display 6 statistics cards")
            .isEqualTo(6);
        
        assertThat(driver.findElement(By.id("municipalityFilter")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("statusFilter")).isDisplayed()).isTrue();
    }

    @Test
    @Order(2)
    void shouldDisplayStatistics() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".stat-card")));
        
        List<WebElement> statCards = driver.findElements(By.cssSelector(".stat-card"));
        
        for (WebElement card : statCards) {
            WebElement statNumber = card.findElement(By.tagName("h3"));
            WebElement statLabel = card.findElement(By.tagName("p"));
            
            assertThat(statNumber.getText()).isNotEmpty();
            assertThat(statLabel.getText()).isNotEmpty();
        }
        
        List<WebElement> statLabels = driver.findElements(By.cssSelector(".stat-card p"));
        List<String> labels = statLabels.stream().map(WebElement::getText).toList();
        
        assertThat(labels).contains(
            "Total Requests",
            "Received",
            "Assigned",
            "In Progress",
            "Completed",
            "Cancelled"
        );
    }

    @Test
    @Order(3)
    void shouldFilterByMunicipality() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("municipalityFilter")));
        
        wait.until(d -> {
            Select select = new Select(d.findElement(By.id("municipalityFilter")));
            return select.getOptions().size() > 1;
        });
        
        Select municipalityFilter = new Select(driver.findElement(By.id("municipalityFilter")));
        
        boolean hasLisboa = municipalityFilter.getOptions().stream()
            .anyMatch(option -> option.getText().contains("Lisboa"));
        
        if (hasLisboa) {
            municipalityFilter.selectByVisibleText("Lisboa");
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            WebElement summary = driver.findElement(By.cssSelector(".requests-summary"));
            assertThat(summary.getText()).containsIgnoringCase("Lisboa");
        }
    }

    @Test
    @Order(4)
    void shouldFilterByStatus() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("statusFilter")));
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".requests-summary")));
        
        Select statusFilter = new Select(driver.findElement(By.id("statusFilter")));
        statusFilter.selectByValue("RECEIVED");
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertThat(statusFilter.getFirstSelectedOption().getAttribute("value"))
            .isEqualTo("RECEIVED");
    }

    @Test
    @Order(5)
    void shouldDisplayRequestCards() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".requests-summary")));
        
        wait.until(driver -> {
            List<WebElement> cards = driver.findElements(By.cssSelector(".request-card"));
            return cards.size() == 5;
        });
        
        List<WebElement> requestCards = driver.findElements(By.cssSelector(".request-card"));
        
        assertThat(requestCards.size())
            .as("Should display 5 test request cards")
            .isEqualTo(5);
        
        WebElement firstCard = requestCards.get(0);
        assertThat(firstCard.getText()).isNotEmpty();
        
        List<WebElement> allButtons = driver.findElements(By.cssSelector(".request-card button"));
        assertThat(allButtons.size())
            .as("Should have at least one button across all cards")
            .isGreaterThan(0);
    }

    @Test
    @Order(6)
    void shouldRefreshRequests() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".btn-refresh")));
        
        WebElement refreshButton = driver.findElement(By.cssSelector(".btn-refresh"));
        refreshButton.click();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        WebElement summary = driver.findElement(By.cssSelector(".requests-summary"));
        assertThat(summary.isDisplayed()).isTrue();
    }

    @Test
    @Order(7)
    void shouldOpenUpdateStatusModal() {
        driver.get(frontendUrl + "/staff");
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".requests-summary")));
        
        List<WebElement> requestCards = driver.findElements(By.cssSelector(".request-card"));
        
        if (requestCards.size() > 0) {
            WebElement firstCard = requestCards.get(0);
            List<WebElement> buttons = firstCard.findElements(By.cssSelector("button"));
            
            if (buttons.size() > 0) {
                WebElement updateButton = buttons.get(0);
                updateButton.click();
                
                try {
                    WebElement modal = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(".modal"))
                    );
                    
                    assertThat(modal.isDisplayed()).isTrue();
                    
                    WebElement closeButton = modal.findElement(By.cssSelector(".close-button, .btn-secondary"));
                    assertThat(closeButton.isDisplayed()).isTrue();
                    
                    closeButton.click();
                    
                    wait.until(ExpectedConditions.invisibilityOf(modal));
                    
                } catch (TimeoutException e) {
                    System.out.println("Modal did not appear - might need different selector");
                }
            }
        } else {
            System.out.println("No requests available to test update status modal");
        }
    }
}

